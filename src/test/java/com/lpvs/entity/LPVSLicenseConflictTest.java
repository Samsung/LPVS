/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LPVSLicenseConflictTest {

    LPVSLicense lpvsLicense1, lpvsLicense2;
    LPVSLicenseConflict licenseConflict;
    final long baseLicenseId1 = 1234567890L;
    final String baseLicenseName1 = "licenseName1";
    final String baseSpdxId1 = "spdxId1";
    final String baseAccess1 = "access1";
    final String baseChecklistUrl1 = "checklistUrl1";

    final long baseLicenseId2 = 234567890L;
    final String baseLicenseName2 = "licenseName2";
    final String baseSpdxId2 = "spdxId2";
    final String baseAccess2 = "access2";
    final String baseChecklistUrl2 = "checklistUrl2";

    @BeforeEach
    void setUp() {
        lpvsLicense1 = new LPVSLicense(baseLicenseId1,
                baseLicenseName1,
                baseSpdxId1,
                baseAccess1,
                baseChecklistUrl1);
        lpvsLicense2 = new LPVSLicense(baseLicenseId2,
                baseLicenseName2,
                baseSpdxId2,
                baseAccess2,
                baseChecklistUrl2);
        licenseConflict = new LPVSLicenseConflict();
        licenseConflict.setConflictId(1L);
        licenseConflict.setConflictLicense(lpvsLicense1);
        licenseConflict.setRepositoryLicense(lpvsLicense2);
    }

    @Test
    public void constructorAndGettersTest() {
        assertEquals(licenseConflict.getConflictId(), 1L);
        assertEquals(licenseConflict.getConflictLicense(), lpvsLicense1);
        assertEquals(licenseConflict.getRepositoryLicense(), lpvsLicense2);
    }

    @Test
    public void setLicenseConflictIdTest() {
        final long newActualValue = 0L;
        assertEquals(licenseConflict.getConflictId(), 1L);
        licenseConflict.setConflictId(newActualValue);
        assertNotEquals(licenseConflict.getConflictId(), 1L);
        assertEquals(licenseConflict.getConflictId(), newActualValue);
    }

    @Test
    public void setConflictLicenseTest() {
        final String newActualName = "NewName";
        assertEquals(licenseConflict.getConflictLicense(), lpvsLicense1);
        lpvsLicense1.setLicenseName(newActualName);
        licenseConflict.setConflictLicense(lpvsLicense1);
        assertEquals(licenseConflict.getConflictLicense().getLicenseName(), newActualName);
    }

    @Test
    public void setRepositoryLicenseTest() {
        final String newActualName = "NewName";
        assertEquals(licenseConflict.getRepositoryLicense(), lpvsLicense2);
        lpvsLicense2.setLicenseName(newActualName);
        licenseConflict.setRepositoryLicense(lpvsLicense2);
        assertEquals(licenseConflict.getRepositoryLicense().getLicenseName(), newActualName);
    }
}
