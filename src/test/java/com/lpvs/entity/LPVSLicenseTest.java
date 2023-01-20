/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSLicenseTest {
    LPVSLicense lpvsLicense;
    final long baseLicenseId = 1234567890L;
    final String baseLicenseName = "licenseName";
    final String baseSpdxId = "spdxId";
    final String baseAccess = "access";
    final String baseChecklistUrl = "checklistUrl";

    @BeforeEach
    void setUp() {
        lpvsLicense = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseChecklistUrl);
    }

    @Test
    public void constructorAndGettersTest() {
        assertEquals(lpvsLicense.getLicenseId(), baseLicenseId);
        assertEquals(lpvsLicense.getLicenseName(), baseLicenseName);
        assertEquals(lpvsLicense.getSpdxId(), baseSpdxId);
        assertEquals(lpvsLicense.getAccess(), baseAccess);
        assertEquals(lpvsLicense.getChecklistUrl(), baseChecklistUrl);
    }

    @Test
    public void setLicenseIdTest() {
        final long newActualValue = 0L;
        assertEquals(lpvsLicense.getLicenseId(), baseLicenseId);
        lpvsLicense.setLicenseId(newActualValue);
        assertNotEquals(lpvsLicense.getLicenseId(), baseLicenseId);
        assertEquals(lpvsLicense.getLicenseId(), newActualValue);
    }

    @Test
    public void setLicenseNameTest() {
        final String newActualName = "NewName";
        assertEquals(lpvsLicense.getLicenseName(), baseLicenseName);
        lpvsLicense.setLicenseName(newActualName);
        assertNotEquals(lpvsLicense.getLicenseName(), baseLicenseName);
        assertEquals(lpvsLicense.getLicenseName(), newActualName);
    }

    @Test
    public void setSpdxIdTest() {
        final String newActualSpdxId = "NewSpdxId";
        assertEquals(lpvsLicense.getSpdxId(), baseSpdxId);
        lpvsLicense.setSpdxId(newActualSpdxId);
        assertNotEquals(lpvsLicense.getSpdxId(), baseSpdxId);
        assertEquals(lpvsLicense.getSpdxId(), newActualSpdxId);
    }

    @Test
    public void setAccessTest() {
        final String newActualAccess = "newAccess";
        assertEquals(lpvsLicense.getAccess(), baseAccess);
        lpvsLicense.setAccess(newActualAccess);
        assertNotEquals(lpvsLicense.getAccess(), baseAccess);
        assertEquals(lpvsLicense.getAccess(), newActualAccess);
    }

    @Test
    public void setChecklistUrlTest() {
        final String newActualCheckUrl = "NewCheckUrl";
        assertEquals(lpvsLicense.getChecklistUrl(), baseChecklistUrl);
        lpvsLicense.setChecklistUrl(newActualCheckUrl);
        assertNotEquals(lpvsLicense.getChecklistUrl(), baseChecklistUrl);
        assertEquals(lpvsLicense.getChecklistUrl(), newActualCheckUrl);
    }
}