/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.dashboard;

import com.lpvs.entity.enums.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DashboardElementsByDateTest {

    private DashboardElementsByDate dashboardData;

    @BeforeEach
    public void setUp() {
        Map<Grade, Integer> riskGradeMap = new HashMap<>();
        riskGradeMap.put(Grade.LOW, 5);
        riskGradeMap.put(Grade.MIDDLE, 3);
        dashboardData = new DashboardElementsByDate(LocalDate.of(2023, 10, 1), 10, 2, riskGradeMap);
    }

    @Test
    public void testGetDate() {
        assertEquals(LocalDate.of(2023, 10, 1), dashboardData.getDate());
    }

    @Test
    public void testGetParticipantCount() {
        assertEquals(10, dashboardData.getParticipantCount());
    }

    @Test
    public void testGetPullRequestCount() {
        assertEquals(2, dashboardData.getPullRequestCount());
    }

    @Test
    public void testGetRiskGradeMap() {
        Map<Grade, Integer> expectedRiskGradeMap = new HashMap<>();
        expectedRiskGradeMap.put(Grade.LOW, 5);
        expectedRiskGradeMap.put(Grade.MIDDLE, 3);

        assertEquals(expectedRiskGradeMap, dashboardData.getRiskGradeMap());
    }
}
