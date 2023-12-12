/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

/**
 * Represents the status of a pull request scan in the LPVS system.
 * Each status corresponds to a specific state or outcome of the scanning process.
 */
public enum LPVSPullRequestStatus {

    /**
     * Indicates that access to the pull request is not possible.
     */
    NO_ACCESS("Cannot access the pull request"),

    /**
     * Indicates that license issues have been detected in the pull request.
     */
    ISSUES_DETECTED("License issues detected"),

    /**
     * Indicates an internal error occurred while posting scan results.
     */
    INTERNAL_ERROR("Error while posting results"),

    /**
     * Indicates that the scan of the pull request has been completed.
     */
    COMPLETED("Scan completed"),

    /**
     * Indicates that the scan of the pull request is scheduled and in progress.
     */
    SCANNING("Scan is scheduled");

    /**
     * The string representation of the pull request status.
     */
    private final String status;

    /**
     * Constructs an LPVSPullRequestStatus with the specified status.
     *
     * @param status The string representation of the pull request status.
     */
    LPVSPullRequestStatus(final String status) {
        this.status = status;
    }

    /**
     * Gets the string representation of the pull request status.
     *
     * @return The string representation of the pull request status.
     */
    public String getPullRequestStatus() {
        return status;
    }

    /**
     * Returns a string representation of the pull request status.
     *
     * @return The string representation of the pull request status.
     */
    @Override
    public String toString() {
        return getPullRequestStatus();
    }
}
