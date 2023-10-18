/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DashBoardElementsTest {

    private DashBoardElements dashBoardElements;

    @BeforeEach
    public void setUp() {
        Map<LocalDate, DashboardElementsByDate> dashboardElementsMap = new HashMap<>();
        dashboardElementsMap.put(LocalDate.of(2023, 10, 1), new DashboardElementsByDate(LocalDate.of(2023, 10, 1), 10, 2, new HashMap<>()));
        dashboardElementsMap.put(LocalDate.of(2023, 10, 2), new DashboardElementsByDate(LocalDate.of(2023, 10, 2), 15, 5, new HashMap<>()));
        dashBoardElements = new DashBoardElements(dashboardElementsMap);
    }

    @Test
    public void testGetDashboardElementsMap() {
        Map<LocalDate, DashboardElementsByDate> expectedMap = new HashMap<>();
        expectedMap.put(LocalDate.of(2023, 10, 1), new DashboardElementsByDate(LocalDate.of(2023, 10, 1), 10, 2, new HashMap<>()));
        expectedMap.put(LocalDate.of(2023, 10, 2), new DashboardElementsByDate(LocalDate.of(2023, 10, 2), 15, 5, new HashMap<>()));
        assertEquals(expectedMap, dashBoardElements.getDashboardElementsMap());
    }
}
