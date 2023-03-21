/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service.scanner.scanoss;

import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.util.LPVSWebhookUtil;
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
import java.util.Objects;

public class LPVSScanossDetectServiceTest {
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
        LPVSScanossDetectService scanossDetectService = new LPVSScanossDetectService();
        LPVSLicenseService licenseService = Mockito.mock(LPVSLicenseService.class);
        LPVSGitHubService gitHubService = Mockito.mock(LPVSGitHubService.class);
        String licenseConflictsSource = "scanner";
        ReflectionTestUtils.setField(scanossDetectService, "licenseService", licenseService);
        ReflectionTestUtils.setField(scanossDetectService, "gitHubService", gitHubService);
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("B");
        scanossDetectService.checkLicenses(webhookConfig);
        ReflectionTestUtils.setField(licenseService, "licenseConflictsSource", licenseConflictsSource);
        Mockito.when(licenseService.findLicenseBySPDX("MIT")).thenReturn(new LPVSLicense(){{
            setLicenseName("MIT");
            setLicenseId(1L);
            setSpdxId("MIT");
        }});
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }
}
