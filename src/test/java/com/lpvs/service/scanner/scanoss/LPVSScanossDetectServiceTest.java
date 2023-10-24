/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service.scanner.scanoss;

import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.LPVSStatisticsService;
import com.lpvs.util.LPVSWebhookUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class LPVSScanossDetectServiceTest {
    @InjectMocks
    private LPVSScanossDetectService scanossDetectService;

    @Mock
    private LPVSLicenseService licenseService;

    @Mock
    private LPVSGitHubService gitHubService;

    @Mock
    private LPVSLicenseRepository lpvsLicenseRepository;

    private String userHome;
    @BeforeEach
    public void setUp() throws URISyntaxException, IOException {
        MockitoAnnotations.openMocks(this);
        if (!(new File(System.getProperty("user.home")
                + "/Results/A").exists())) {
            (new File("Results/A")).mkdirs();
        }
        // File f = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("A_B.txt")).toURI());
        Files.copy(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("A_B.json")).toURI()),
                Paths.get("Results/A/B.json"), StandardCopyOption.REPLACE_EXISTING);
        userHome = System.getProperty("user.home");
        System.setProperty("user.home", "C:\\Users\\v.kerimov\\Documents\\v-kerimov\\LPVS");
    }

    @AfterEach
    public void tearDown() {
        if ((new File("Results/A")).exists()) {
            (new File("Results/A")).delete();
            (new File("Results")).delete();
        }
        System.setProperty("user.home", userHome);
    }

    @Test
    public void testWithNonNullHeadCommitSHA() {
        String licenseConflictsSource = "scanner";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("B");
        Mockito.when(licenseService.findLicenseBySPDX("MIT")).thenReturn(new LPVSLicense(){{
            setLicenseName("MIT");
            setLicenseId(1L);
            setSpdxId("MIT");
        }});
        ReflectionTestUtils.setField(licenseService, "licenseConflictsSource", licenseConflictsSource);
        /*webhookConfig.getPullRequestUrl()*/
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }

    @Test
    public void testWithNullHeadCommitSHA() {
        String licenseConflictsSource = "scanner";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn(null);
        Mockito.when(webhookConfig.getPullRequestUrl()).thenReturn("A/B");
        Mockito.when(licenseService.findLicenseBySPDX("MIT")).thenReturn(new LPVSLicense(){{
            setLicenseName("MIT");
            setLicenseId(1L);
            setSpdxId("MIT");
        }});
        ReflectionTestUtils.setField(licenseService, "licenseConflictsSource", licenseConflictsSource);
        /*webhookConfig.getPullRequestUrl()*/
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }
}
