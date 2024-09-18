/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import com.lpvs.service.LPVSLicenseService;
import com.lpvs.util.LPVSExitHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LPVSConflictTest {

    private LPVSExitHandler exitHandler;
    final String license1 = "license1";
    final String license2 = "license2";
    final LPVSConflict<String, String> base_conf_11 = new LPVSConflict<>(license1, license1);
    final LPVSConflict<String, String> base_conf_12 = new LPVSConflict<>(license1, license2);
    final LPVSConflict<String, String> base_conf_21 = new LPVSConflict<>(license2, license1);
    final LPVSConflict<String, String> base_conf_22 = new LPVSConflict<>(license2, license2);

    @Test
    public void findConflictsEmptyScanResults() {
        List<LPVSFile> scanResults = new ArrayList<>();
        LPVSLicenseService mockLicenseService = new LPVSLicenseService(null, exitHandler);
        LPVSQueue webhookConfig = new LPVSQueue(); // licenseConflicts = new ArrayList<>();
        assertEquals(
                new ArrayList<>(), mockLicenseService.findConflicts(webhookConfig, scanResults));
    }

    @Test
    public void conflictEquals() {
        String license1 = "license1";
        String license2 = "license2";

        LPVSConflict<String, String> conf = new LPVSConflict<>(license1, license2);
        assertNotEquals(conf.getL1(), conf.getL2());
    }

    @Test
    public void conflictEqualNotEqualsTest() {
        final String license1 = "license1";
        final String license2 = "license2";
        final LPVSConflict<String, String> conf_1_2 = new LPVSConflict<>(license1, license2);

        assertEquals(conf_1_2, conf_1_2);
        assertEquals(conf_1_2, base_conf_12);
        assertEquals(conf_1_2, base_conf_21);

        assertNotEquals(conf_1_2, base_conf_22);
        assertNotEquals(conf_1_2, base_conf_11);
        assertNotEquals(license1, conf_1_2);
        assertNotEquals(null, conf_1_2);
    }

    @Test
    public void hashTest() {
        String license1 = "license1";
        String license2 = "license2";
        assertEquals(Objects.hash(license1, license2), base_conf_12.hashCode());
        assertNotEquals(Objects.hash(license1, license2), base_conf_11.hashCode());
        assertNotEquals(Objects.hash(license1, license2), base_conf_21.hashCode());
        assertNotEquals(Objects.hash(license1, license2), base_conf_22.hashCode());
    }
}
