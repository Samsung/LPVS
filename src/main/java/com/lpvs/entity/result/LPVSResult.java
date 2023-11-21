/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LPVSResult {
    private List<LPVSResultFile> lpvsResultFileList;
    private LPVSResultInfo lpvsResultInfo;
    private Long count;

    private Map<String, Integer> licenseCountMap;

    private String pullNumber;

    private Boolean hasIssue;
}
