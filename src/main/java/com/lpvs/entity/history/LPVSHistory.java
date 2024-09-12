/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.history;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a historical record in the LPVS system.
 * Each instance of this class encapsulates information about a specific historical event or scan.
 * The information includes details such as the scan date, repository name, pull request ID, URL,
 * status, sender, pull request number, and whether the scan has detected any issues.
 * <p>
 * Instances of this class are used to store and retrieve historical data related to scans
 * of pull requests in the LPVS system.
 * </p>
 */
@Getter
@AllArgsConstructor
public class LPVSHistory {

    /**
     * The date when the scan occurred.
     */
    private String scanDate;

    /**
     * The name of the repository associated with the scan.
     */
    private String repositoryName;

    /**
     * The ID of the pull request.
     */
    private Long pullRequestId;

    /**
     * The URL associated with the scan.
     */
    private String url;

    /**
     * The status of the scan.
     */
    private String status;

    /**
     * The user who initiated or triggered the scan.
     */
    private String sender;

    /**
     * The pull request number in the format "pull/number".
     */
    private String pullNumber;

    /**
     * Indicates whether the scan has detected any issues.
     */
    private Boolean hasIssue;
}
