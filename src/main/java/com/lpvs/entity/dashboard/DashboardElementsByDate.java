/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.dashboard;

import com.lpvs.entity.enums.Grade;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

/**
 * Represents dashboard elements grouped by date in the LPVS system.
 * This class includes information such as date, participant count, pull request count,
 * and a map of risk grades and their respective counts.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class DashboardElementsByDate {

    /**
     * The date associated with the dashboard elements.
     */
    private LocalDate date;

    /**
     * The count of participants on the specified date.
     */
    private int participantCount;

    /**
     * The count of pull requests on the specified date.
     */
    private int pullRequestCount;

    /**
     * A map representing the counts of risk grades associated with the dashboard elements.
     */
    private Map<Grade, Integer> riskGradeMap;
}
