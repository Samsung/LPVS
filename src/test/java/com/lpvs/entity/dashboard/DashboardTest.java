/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DashboardTest {

    private Dashboard dashboard;

    @BeforeEach
    public void setUp() {
        Map<String, Integer> licenseCountMap = new HashMap<>();
        licenseCountMap.put("License1", 10);
        licenseCountMap.put("License2", 5);
        dashboard = new Dashboard("Test Dashboard", licenseCountMap, 100, 20, 30, 50, 10, null);
    }

    @Test
    public void testGetName() {
        assertEquals("Test Dashboard", dashboard.getName());
    }

    @Test
    public void testGetLicenseCountMap() {
        Map<String, Integer> expectedMap = new HashMap<>();
        expectedMap.put("License1", 10);
        expectedMap.put("License2", 5);

        assertEquals(expectedMap, dashboard.getLicenseCountMap());
    }

    @Test
    public void testTotalDetectionCount() {
        assertEquals(100, dashboard.getTotalDetectionCount());
    }

    @Test
    public void testHighSimilarityCount() {
        assertEquals(20, dashboard.getHighSimilarityCount());
    }

    @Test
    public void testTotalIssueCount() {
        assertEquals(30, dashboard.getTotalIssueCount());
    }

    @Test
    public void testTotalParticipantsCount() {
        assertEquals(50, dashboard.getTotalParticipantsCount());
    }

    @Test
    public void testTotalRepositoryCount() {
        assertEquals(10, dashboard.getTotalRepositoryCount());
    }
}
