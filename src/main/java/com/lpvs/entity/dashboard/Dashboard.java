/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class Dashboard {
    private String name;
    private Map<String, Integer> licenseCountMap;

    private int totalDetectionCount;
    private int highSimilarityCount;
    private int totalIssueCount;
    private int totalParticipantsCount;
    private int totalRepositoryCount;

    private List<DashboardElementsByDate> dashboardElementsByDates;
}
