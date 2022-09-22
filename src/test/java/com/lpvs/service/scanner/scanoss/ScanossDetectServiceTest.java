/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service.scanner.scanoss;

import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.service.GitHubService;
import com.lpvs.service.LicenseService;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;

public class ScanossDetectServiceTest {
    @BeforeEach
    public void setUp() throws URISyntaxException, IOException {
        if (!(new File("RESULTS").exists())) {
            new File("RESULTS").mkdir();
        }
        // File f = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("A_B.txt")).toURI());
        Files.copy(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("A_B.json")).toURI()),
                Paths.get("RESULTS/A_B.json"), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    public void tearDown() {
        if ((new File("RESULTS")).exists()) {
            new File("RESULTS").delete();
        }
    }

    @Test
    public void test() {
        ScanossDetectService scanossDetectService = new ScanossDetectService();
        LicenseService licenseService = Mockito.mock(LicenseService.class);
        GitHubService gitHubService = Mockito.mock(GitHubService.class);
        String licenseConflictsSource = "scanner";
        ReflectionTestUtils.setField(scanossDetectService, "licenseService", licenseService);
        ReflectionTestUtils.setField(scanossDetectService, "gitHubService", gitHubService);
        WebhookConfig webhookConfig = Mockito.mock(WebhookConfig.class);
        Mockito.when(webhookConfig.getRepositoryName()).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("B");
        scanossDetectService.checkLicenses(webhookConfig);
        ReflectionTestUtils.setField(licenseService, "licenseConflictsSource", licenseConflictsSource);
        Mockito.when(licenseService.findLicenseBySPDX("MIT")).thenReturn(new LPVSLicense(){{
            setLicenseName("MIT");
            setLicenseId(1L);
            setSpdxId("MIT");
            setIncompatibleWith(new ArrayList<String>(){{
                add("LGPL-2.1-or-later");
            }});
        }});
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }
}
