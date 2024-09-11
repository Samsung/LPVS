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

import java.util.List;
import java.util.Map;

/**
 * Represents the overall results of a scan in the LPVS system.
 * Each instance of this class encapsulates information about the scan results, including
 * a list of result files, result information, total count, license count map, pull request number,
 * and whether the scan has detected any issues.
 * <p>
 * Instances of this class provide a comprehensive summary of the results obtained from a scan in the LPVS system.
 * The information includes details about individual result files, overall result information,
 * the total count of results, a map of license counts, the associated pull request number, and an indication
 * of whether any issues were detected during the scan.
 * </p>
 */
@Getter
@AllArgsConstructor
public class LPVSResult {

    /**
     * The list of {@link LPVSResultFile} objects representing individual result files.
     */
    private List<LPVSResultFile> lpvsResultFileList;

    /**
     * The information about the overall results of the scan.
     */
    private LPVSResultInfo lpvsResultInfo;

    /**
     * The total count of results.
     */
    private Long count;

    /**
     * A map containing license counts, where the key is the license name or identifier,
     * and the value is the count of occurrences.
     */
    private Map<String, Integer> licenseCountMap;

    /**
     * The pull request number associated with the scan.
     */
    private String pullNumber;

    /**
     * Indicates whether the scan has detected any issues.
     */
    private Boolean hasIssue;
}
