/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.dashboard;

import com.lpvs.entity.enums.Grade;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter @AllArgsConstructor
public class DashboardElementsByDate {
    private LocalDate date;
    private int participantCount;
    private int pullRequestCount;
    private Map<Grade, Integer> riskGradeMap;
}
