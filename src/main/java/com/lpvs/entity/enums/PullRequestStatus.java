/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.enums;

public enum PullRequestStatus {
    NO_ACCESS("Cannot access the pull request"),
    ISSUES_DETECTED("License issues detected"),
    INTERNAL_ERROR("Error while posting results"),
    COMPLETED("Scan completed");

    private final String status;

    PullRequestStatus(final String status) {
        this.status = status;
    }

    public String getPullRequestStatus() {
        return status;
    }

    @Override
    public String toString() {
        return getPullRequestStatus();
    }
}
