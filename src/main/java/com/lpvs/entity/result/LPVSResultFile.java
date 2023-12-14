/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents information about a file and its matching details in the results of a scan
 * in the LPVS system.
 * Each instance of this class encapsulates details such as the file ID, file path,
 * component file URL, match line, match value, license status, and SPDX identifier.
 * <p>
 * Instances of this class are used to provide detailed information about individual files
 * that were part of the scan results in the LPVS system. The information includes the file path,
 * component file URL, match details, license status, and SPDX identifier for the associated license.
 * </p>
 */
@Getter
@AllArgsConstructor
public class LPVSResultFile {

    /**
     * The unique identifier for the file.
     */
    private Long id;

    /**
     * The path of the file within the repository.
     */
    private String path;

    /**
     * The URL of the component file associated with this result.
     */
    private String componentFileUrl;

    /**
     * The line in the file where the match occurred.
     */
    private String matchLine;

    /**
     * The match type during the scan.
     */
    private String matchValue;

    /**
     * The status of the license associated with the file.
     */
    private String status; // license.licenseUsage

    /**
     * The SPDX identifier of the license associated with the file.
     */
    private String licenseSpdx; // license
}
