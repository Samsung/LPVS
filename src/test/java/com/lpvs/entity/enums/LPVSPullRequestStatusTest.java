/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPVSPullRequestStatusTest {

    @Test
    public void testGetPullRequestStatus() {
        assertEquals("Cannot access the pull request", LPVSPullRequestStatus.NO_ACCESS.getPullRequestStatus());
        assertEquals("License issues detected", LPVSPullRequestStatus.ISSUES_DETECTED.getPullRequestStatus());
        assertEquals("Error while posting results", LPVSPullRequestStatus.INTERNAL_ERROR.getPullRequestStatus());
        assertEquals("Scan completed", LPVSPullRequestStatus.COMPLETED.getPullRequestStatus());
        assertEquals("Scan is scheduled", LPVSPullRequestStatus.SCANNING.getPullRequestStatus());
    }

    @Test
    public void testToString() {
        assertEquals("Cannot access the pull request", LPVSPullRequestStatus.NO_ACCESS.toString());
        assertEquals("License issues detected", LPVSPullRequestStatus.ISSUES_DETECTED.toString());
        assertEquals("Error while posting results", LPVSPullRequestStatus.INTERNAL_ERROR.toString());
        assertEquals("Scan completed", LPVSPullRequestStatus.COMPLETED.toString());
        assertEquals("Scan is scheduled", LPVSPullRequestStatus.SCANNING.toString());
    }

    @Test
    public void testEnumValues() {
        LPVSPullRequestStatus[] values = LPVSPullRequestStatus.values();
        assertEquals(5, values.length);
        assertEquals(LPVSPullRequestStatus.NO_ACCESS, values[0]);
        assertEquals(LPVSPullRequestStatus.ISSUES_DETECTED, values[1]);
        assertEquals(LPVSPullRequestStatus.INTERNAL_ERROR, values[2]);
        assertEquals(LPVSPullRequestStatus.COMPLETED, values[3]);
        assertEquals(LPVSPullRequestStatus.SCANNING, values[4]);
    }
}
