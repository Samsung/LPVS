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
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.util.LPVSWebhookUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
        MockitoAnnotations.openMocks(this);
        String resourcePath = "A_B.json";
        String destinationPath =
                System.getProperty("user.home") + File.separator + "Results" + File.separator + "C";
        if (!(new File(destinationPath).exists())) {
            new File(destinationPath).mkdirs();
        }
        Files.copy(
                Paths.get(
                        Objects.requireNonNull(
                                        getClass().getClassLoader().getResource(resourcePath))
                                .toURI()),
                Paths.get(destinationPath + File.separator + resourcePath),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    public void tearDown() {
        if ((new File(System.getProperty("user.home") + File.separator + "Results")).exists()) {
            new File(System.getProperty("user.home") + File.separator + "Results").delete();
        }
    }

    @Test
    public void testCheckLicense() {
        LPVSLicenseService licenseService = Mockito.mock(LPVSLicenseService.class);
        LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
        LPVSScanossDetectService scanossDetectService =
                new LPVSScanossDetectService(false, licenseService, lpvsLicenseRepository);
        String licenseConflictsSource = "scanner";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("C");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("A_B");
        Mockito.when(lpvsLicenseRepository.save(Mockito.any(LPVSLicense.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        ReflectionTestUtils.setField(
                licenseService, "licenseConflictsSource", licenseConflictsSource);
        Mockito.when(licenseService.findLicenseBySPDX("MIT"))
                .thenReturn(
                        new LPVSLicense() {
                            {
                                setLicenseName("MIT");
                                setLicenseId(1L);
                                setSpdxId("MIT");
                            }
                        });
        scanossDetectService.checkLicenses(webhookConfig);
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }

    @Test
    public void testWithNullHeadCommitSHA() {
        LPVSLicenseService licenseService = Mockito.mock(LPVSLicenseService.class);
        LPVSLicenseRepository lpvsLicenseRepository = Mockito.mock(LPVSLicenseRepository.class);
        LPVSScanossDetectService scanossDetectService =
                new LPVSScanossDetectService(false, licenseService, lpvsLicenseRepository);
        String licenseConflictsSource = "scanner";
        LPVSQueue webhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(LPVSWebhookUtil.getRepositoryName(webhookConfig)).thenReturn("A");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn(null);
        Mockito.when(webhookConfig.getPullRequestUrl()).thenReturn("A/B");
        Mockito.when(licenseService.findLicenseBySPDX("MIT"))
                .thenReturn(
                        new LPVSLicense() {
                            {
                                setLicenseName("MIT");
                                setLicenseId(1L);
                                setSpdxId("MIT");
                            }
                        });
        ReflectionTestUtils.setField(
                licenseService, "licenseConflictsSource", licenseConflictsSource);
        Assertions.assertNotNull(scanossDetectService.checkLicenses(webhookConfig));
    }
}
