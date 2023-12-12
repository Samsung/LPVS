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

/**
 * Represents a dashboard in the LPVS system.
 * This class encapsulates various metrics and elements for visualizing system information.
 */
@Getter
@AllArgsConstructor
public class Dashboard {

    /**
     * The name of the dashboard.
     */
    private String name;

    /**
     * A map associating license names with their respective counts.
     */
    private Map<String, Integer> licenseCountMap;

    /**
     * The total count of license detections across the system.
     */
    private int totalDetectionCount;

    /**
     * The count of high similarity license detections.
     */
    private int highSimilarityCount;

    /**
     * The total count of issues identified in the system.
     */
    private int totalIssueCount;

    /**
     * The total count of participants in the system.
     */
    private int totalParticipantsCount;

    /**
     * The total count of repositories in the system.
     */
    private int totalRepositoryCount;

    /**
     * A list of dashboard elements grouped by date.
     */
    private List<DashboardElementsByDate> dashboardElementsByDates;
}
