# Quick Start Guide & Build

## Contents

### [Quick Start Guide](#quick-start-guide-1)

1. [Setting up your project to interact with _LPVS_](#1-setting-up-your-project-to-interact-with-lpvs)
2. [Using pre-built _LPVS_ Docker images](#2-using-pre-built-lpvs-docker-images)
3. [Setting up your project to interact with _LPVS_](#3-setting-up-your-project-to-interact-with-lpvs)

---

## Quick Start Guide

### 1. Setting up your project to interact with _LPVS_

To enable _LPVS_ license scanning for your project, you need to set up GitHub Webhooks:

1.1 Create a personal github access token (`personal-token`):

- Follow the instructions [here](#https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-fine-grained-personal-access-token) to create a personal access token with the necessary permissions.

> [!NOTE]
> Pay attention that the token must be copied immediately after creation, because you will not be able to see it later!!

1.2 Get a personal ngrok auth token to expose your local service (`auth-token`):

- The ngrok agent authenticates with an authtoken. Your authtoken is available on the ngrok [dashboard](https://dashboard.ngrok.com/get-started/your-authtoken).

---

### 2. Using pre-built _LPVS_ Docker images

This section explains how to download and run pre-built _LPVS_ Docker image with ngrok reverse proxy.

For the Docker deployment scenario, you may need to set additional parameters described in [docker section](/doc/docs/user-guide/service/docker.md).

#### 2.1 Setting up _LPVS_ Docker environment variables

2.1.1 Open `docker-compose.yml` file.

2.1.2 In the `environment` part of the `lpvs` service, find `## Github data for fetching code` and fill in the github `login` and personal `token` that was generated earlier

```yaml
- github.login=<github-login>
- github.token=<personal-token>
```

2.1.3 In the `environment` part of the `ngrok` service, find `## Ngrok Auth token` and fill personal `token` from [Ngrok portal](https://dashboard.ngrok.com/get-started/your-authtoken).

```yaml
- NGROK_AUTHTOKEN=<auth-token>
```

#### 2.2 Running _LPVS_ and MySQL Docker images with Docker Compose

2.2.1 Start the _LPVS_ services:

```bash
docker compose -f docker-compose-quick.yml up -d
```

To stop the _LPVS_ services, use next command:

```bash
docker compose -f docker-compose-quick.yml down
```

### 3. Setting up your project to interact with _LPVS_

3.1 Configure the [webhook](/doc/docs/user-guide/service/webhook.md#configure-the-webhook-in-your-github-repository-settings) in your GitHub repository settings:

- Go to `Settings` -> `Webhooks`.
- Click on `Add webhook`.
- Fill in the `Payload URL` with: `<Tunnel URL>:7896/webhooks`.
  > The `Tunnel URL` can be found on localhost: `http://127.0.0.1:4040/`.
- Specify the content type as `application/json`.
- Fill in the `Secret` field with the passphrase: `LPVS`.
- Select `Let me select individual events` -> `Pull requests` (make sure only `Pull requests` is selected).
- Set the webhook to `Active`.
- Click `Add Webhook`.

Configuration from your project side is now complete!
You can now create a new pull request or update an existing one with commits. _LPVS_ will automatically start scanning and provide comments about the licenses found in the project.
