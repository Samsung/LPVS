/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;

/**
 * Represents information about the results of a scan in the LPVS system.
 * Each instance of this class encapsulates details such as the scan ID, scan date, repository name,
 * status of the scan, and a list of detected licenses.
 * <p>
 * Instances of this class are used to convey essential information about the results of a scan
 * performed by the LPVS system. The information includes key details for identifying and understanding
 * the outcome of the scan, including the repository name, scan date, and the licenses that were detected.
 * </p>
 */
@Getter
@AllArgsConstructor
public class LPVSResultInfo {

    /**
     * The unique identifier for the scan.
     */
    private Long id;

    /**
     * The date when the scan occurred.
     */
    private Date scanDate;

    /**
     * The name of the repository associated with the scan.
     */
    private String repositoryName;

    /**
     * The status of the scan.
     */
    private String status;

    /**
     * The list of detected licenses during the scan.
     */
    private List<String> detectedLicenses;
}
