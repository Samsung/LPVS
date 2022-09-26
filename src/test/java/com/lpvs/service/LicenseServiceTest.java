/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.config.WebhookConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class LicenseServiceTest {

    final String license1 = "license1";
    final String license2 = "license2";

    final LicenseService.Conflict<String, String> base_conf_11 = new LicenseService().new Conflict<>(license1, license1);
    final LicenseService.Conflict<String, String> base_conf_12 = new LicenseService().new Conflict<>(license1, license2);
    final LicenseService.Conflict<String, String> base_conf_21 = new LicenseService().new Conflict<>(license2, license1);
    final LicenseService.Conflict<String, String> base_conf_22 = new LicenseService().new Conflict<>(license2, license2);

    @Test
    public void findConflictsEmptyScanResults() {
        List<LPVSFile> scanResults = new ArrayList<>();
        if (scanResults.isEmpty()) {
            System.out.println("is empty");
        }
        LicenseService mockLicenseService = new LicenseService();
        WebhookConfig webhookConfig = new WebhookConfig(); // licenseConflicts = new ArrayList<>();
        assertEquals(null, mockLicenseService.findConflicts(webhookConfig, scanResults));

    }

    @Test
    public void findConflictsLicenseConflictsIsEmpty() {
        List<LPVSFile> scanResults = new ArrayList<>();
        LPVSFile lpvsFile = mock(LPVSFile.class);
        scanResults.add(lpvsFile);
        assertEquals(false, scanResults.isEmpty());

        LicenseService licenseService = new LicenseService();
        try {
            Method andPrivateMethod = LicenseService.class.getDeclaredMethod("init");
            andPrivateMethod.setAccessible(true);
            andPrivateMethod.invoke(licenseService);

            WebhookConfig webhookConfig = new WebhookConfig(); // licenseConflicts = new ArrayList<>();
            assertEquals(null, licenseService.findConflicts(webhookConfig, scanResults));
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals(false, true);
        }
        WebhookConfig webhookConfig = new WebhookConfig();
        assertEquals(null, licenseService.findConflicts(webhookConfig, scanResults));
    }

    @Test
    public void findConflictsMainReturn() {
        List<LPVSFile> scanResults = new ArrayList<>();
        LPVSFile lpvsFile = mock(LPVSFile.class);
        scanResults.add(lpvsFile);
        assertEquals(false, scanResults.isEmpty());

        LicenseService licenseService = new LicenseService();
        licenseService.public_init();
        WebhookConfig webhookConfig = new WebhookConfig();

        List<LicenseService.Conflict<String, String>> foundConflicts = null;
        assertEquals(foundConflicts, licenseService.findConflicts(webhookConfig, scanResults));
    }

    @Test
    public void conflictEquals() {
        String license1 = "license1";
        String license2 = "license2";

        LicenseService.Conflict<String, String> conf = new LicenseService().new Conflict<>(license1, license2);
        assertNotEquals(conf.l1, conf.l2);
    }

    @Test
    public void conflictEqualNotEqualsTest() {
        final String license1 = "license1";
        final String license2 = "license2";
        final LicenseService.Conflict<String, String> conf_1_2 = new LicenseService().new Conflict<>(license1, license2);

        assertEquals(true, conf_1_2.equals(conf_1_2));
        assertEquals(true, conf_1_2.equals(base_conf_12));
        assertEquals(true, conf_1_2.equals(base_conf_21));

        assertEquals(false, conf_1_2.equals(base_conf_22));
        assertEquals(false, conf_1_2.equals(base_conf_11));
        assertEquals(false, conf_1_2.equals(null));
        assertEquals(false, conf_1_2.equals(license1));
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
