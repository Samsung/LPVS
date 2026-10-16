#!/usr/bin/env bash
#
# Copyright (c) 2026, Samsung Electronics Co., Ltd. All rights reserved.
#
# Use of this source code is governed by a MIT license that can be
# found in the LICENSE file.
#
# Integration tests for the LPVS Docker image:
#   1. The container runs as an unprivileged user.
#   2. Single scan (CLI) mode works as that user without network access.
#   3. A Docker volume mounted at the LPVS working directory is writable.
#   4. The service started with docker compose rejects unsigned webhooks and
#      webhooks with path traversal in the head commit SHA.
#
# Usage: IMAGE=<image> [STEPS="1 2 3 4"] .github/scripts/docker-integration-test.sh
# Builds the image first if it doesn't exist or BUILD=1. STEPS selects the tests to run.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IMAGE="${IMAGE:-lpvs:integration-test}"
LPVS_UID=10001
LPVS_HOME=/home/lpvs
PORT="${LPVS_PORT:-17896}"
PROJECT=lpvs-it
SUFFIX="$$"
M2_VOLUME="lpvs-it-m2-${SUFFIX}"
DATA_VOLUME="lpvs-it-data-${SUFFIX}"
STEPS="${STEPS:-1 2 3 4}"
TMP_DIR="$(mktemp -d)"
FAILURES=0

log() { printf '\n== %s\n' "$*"; }
pass() { printf '  PASS: %s\n' "$*"; }
fail() { printf '  FAIL: %s\n' "$*"; FAILURES=$((FAILURES + 1)); }
check() { # check <description> <command...>
    local description="$1"; shift
    if "$@"; then pass "$description"; else fail "$description"; fi
}
step() { [[ " $STEPS " == *" $1 "* ]]; }

compose() {
    docker compose -p "$PROJECT" --env-file "$TMP_DIR/.env" \
        -f "$REPO_ROOT/docker-compose.yml" -f "$TMP_DIR/docker-compose.override.yml" "$@"
}

cleanup() {
    if [ -f "$TMP_DIR/docker-compose.override.yml" ]; then
        compose down -v --remove-orphans >/dev/null 2>&1 || true
    fi
    docker volume rm -f "$M2_VOLUME" "$DATA_VOLUME" >/dev/null 2>&1 || true
    rm -rf "$TMP_DIR"
}
trap cleanup EXIT

if [ "${BUILD:-}" = "1" ] || ! docker image inspect "$IMAGE" >/dev/null 2>&1; then
    log "Building image $IMAGE"
    docker build -t "$IMAGE" "$REPO_ROOT"
fi

# Runs a shell command in the image as the default (unprivileged) user
run_in_image() { docker run --rm --entrypoint sh "$@"; }

# ---------------------------------------------------------------------------
if step 1; then
log "1. Container runs as unprivileged user"
uid="$(docker run --rm --entrypoint id "$IMAGE" -u)"
check "process uid is $LPVS_UID (got $uid)" [ "$uid" = "$LPVS_UID" ]
for path in /etc/cron.d /root /usr/local/bin /usr/local/lib/python3.13/site-packages; do
    check "$path is not writable" \
        run_in_image "$IMAGE" -c "! touch '$path/.probe' 2>/dev/null"
done
check "lpvs.jar exists and is not writable" \
    run_in_image "$IMAGE" -c "[ -f '$LPVS_HOME/lpvs.jar' ] && [ ! -w '$LPVS_HOME/lpvs.jar' ]"
check "scanoss-py runs" run_in_image "$IMAGE" -c "scanoss-py --version >/dev/null"
fi

# ---------------------------------------------------------------------------
if step 2 || step 3; then
# The thin jar downloads its dependencies on first start, so fetch them with network
# access first and then run the scan with the network disabled (no SCANOSS API calls).
docker volume create "$M2_VOLUME" >/dev/null
docker run --rm -u 0 -v "$M2_VOLUME:$LPVS_HOME/.m2" --entrypoint chown "$IMAGE" \
    "$LPVS_UID:$LPVS_UID" "$LPVS_HOME/.m2"
docker run --rm -v "$M2_VOLUME:$LPVS_HOME/.m2" "$IMAGE" \
    java -jar lpvs.jar --thin.dryrun >/dev/null

single_scan() { # single_scan <extra docker run arguments...>
    docker run --rm --network none -v "$M2_VOLUME:$LPVS_HOME/.m2" "$@" \
        --entrypoint sh "$IMAGE" -c '
            mkdir -p /tmp/src && printf "int main() { return 0; }\n" > /tmp/src/test.c
            timeout 300 java -Dspring.profiles.active=singlescan -jar lpvs.jar \
                --local.path=/tmp/src/test.c > /tmp/app.log 2>&1 || true
            cat /tmp/app.log
            echo "--- LPVS directory:"
            find ~/LPVS -maxdepth 2 -exec stat -c "%u %n" {} \;'
}

fi

if step 2; then
log "2. Single scan (CLI) mode as unprivileged user, without network access"
scan_output="$(single_scan)"
check "application started as lpvs" grep -q "started by lpvs" <<<"$scan_output"
check "single scan was triggered" grep -q "Triggered single scan" <<<"$scan_output"
check "no permission errors" \
    bash -c '! grep -qiE "permission denied|AccessDeniedException" <<<"$1"' _ "$scan_output"
check "Projects directory created by uid $LPVS_UID" \
    grep -qE "^$LPVS_UID $LPVS_HOME/LPVS/Projects\$" <<<"$scan_output"
check "Results directory created by uid $LPVS_UID" \
    grep -qE "^$LPVS_UID $LPVS_HOME/LPVS/Results\$" <<<"$scan_output"

fi

# ---------------------------------------------------------------------------
if step 3; then
log "3. Docker volume mounted at $LPVS_HOME/LPVS"
docker volume create "$DATA_VOLUME" >/dev/null
scan_output="$(single_scan -v "$DATA_VOLUME:$LPVS_HOME/LPVS")"
check "no permission errors with mounted volume" \
    bash -c '! grep -qiE "permission denied|AccessDeniedException" <<<"$1"' _ "$scan_output"
check "scan files written to the volume" \
    run_in_image -v "$DATA_VOLUME:/data" "$IMAGE" -c \
    "[ -n \"\$(find /data/Projects -type f -name test.c)\" ]"
fi

# ---------------------------------------------------------------------------
if step 4; then
log "4. Service started with docker compose"
random() { openssl rand -hex 16; }
WEBHOOK_SECRET="$(random)"
cat >"$TMP_DIR/.env" <<EOF
MYSQL_ROOT_PASSWORD=$(random)
MYSQL_USER=lpvs
MYSQL_PASSWORD=$(random)
LPVS_GITHUB_SECRET=$WEBHOOK_SECRET
LPVS_API_KEY=$(random)
EOF
cat >"$TMP_DIR/docker-compose.override.yml" <<EOF
services:
  lpvs:
    image: $IMAGE
    pull_policy: never
    build: !reset null
    restart: "no"
    ports: !override
      - "127.0.0.1:$PORT:7896"
    environment:
      # Required to start in server mode; these tests don't call the GitHub API
      github.token: integration-test-placeholder
  mysqldb:
    restart: "no"
EOF

compose up -d --wait mysqldb
compose up -d lpvs
BASE_URL="http://127.0.0.1:$PORT"

# http_status <curl arguments...>: prints the HTTP status code of the request (000 on error)
http_status() { curl -s -o /dev/null -w '%{http_code}' "$@" || true; }

started=0
for _ in $(seq 1 150); do
    if [ "$(http_status "$BASE_URL/health")" = 200 ]; then started=1; break; fi
    sleep 2
done
if [ "$started" != 1 ]; then
    fail "service did not start within 5 minutes"
    compose logs --tail 100 lpvs || true
    exit 1
fi

# webhook <payload> <signature>: sends a webhook and prints the HTTP status code
webhook() {
    http_status -X POST "$BASE_URL/webhooks" -H "Content-Type: application/json" \
        -H "X-Hub-Signature-256: $2" --data-binary "$1"
}

sign() {
    printf 'sha256=%s' "$(printf '%s' "$1" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" -r \
        | cut -d' ' -f1)"
}

pull_request_payload() { # pull_request_payload <head sha>
    printf '%s' '{"action":"opened","repository":{"html_url":"https://github.com/Samsung/LPVS"},'
    printf '%s' '"pull_request":{"html_url":"https://github.com/Samsung/LPVS/pull/1",'
    printf '%s' '"url":"https://api.github.com/repos/Samsung/LPVS/pulls/1",'
    printf '%s' '"base":{"repo":{"owner":{"login":"Samsung"}}},'
    printf '%s' '"head":{"repo":{"fork":false,"owner":{"login":"Samsung"}},"sha":"'"$1"'"}},'
    printf '%s' '"sender":{"login":"user"}}'
}

status="$(http_status "$BASE_URL/health")"
check "GET /health returns 200 (got $status)" [ "$status" = 200 ]

ping='{"zen":"Keep it logically awesome.","hook_id":1}'
status="$(webhook "$ping" "$(sign "$ping")")"
check "signed ping webhook returns 200 (got $status)" [ "$status" = 200 ]

status="$(webhook "$ping" "sha256=$(printf '0%.0s' $(seq 1 64))")"
check "webhook with wrong signature returns 403 (got $status)" [ "$status" = 403 ]

traversal="$(pull_request_payload "x/../../../../../../tmp/lpvs-it-pwned")"
status="$(webhook "$traversal" "$(sign "$traversal")")"
check "signed webhook with path traversal in SHA returns 400 (got $status)" [ "$status" = 400 ]
check "no directory created outside the LPVS working directory" \
    compose exec -T lpvs sh -c "[ ! -e /tmp/lpvs-it-pwned ]"

status="$(http_status -X POST "$BASE_URL/scan/Samsung/LPVS/1")"
check "single scan without API key returns 403 (got $status)" [ "$status" = 403 ]

status="$(http_status "$BASE_URL/unknown")"
check "unknown endpoint returns 403 (got $status)" [ "$status" = 403 ]

if [ "$FAILURES" -ne 0 ]; then
    log "LPVS logs"
    compose logs --tail 100 lpvs || true
fi
fi

if [ "$FAILURES" -ne 0 ]; then
    log "$FAILURES check(s) failed"
    exit 1
fi
log "All checks passed"
