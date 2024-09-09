/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.webhook;

import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.LPVSQueueService;
import com.lpvs.service.scan.LPVSDetectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;

/**
 * The LPVSWebhookServiceFactory class provides a factory method to create instances of LPVSWebhookService implementations.
 */
@Service
public class LPVSWebhookServiceFactory {

    /**
     * Service for interacting with GitHub.
     */
    @Autowired private LPVSGitHubService gitHubService;

    /**
     * Service for detecting licenses in scanned files.
     */
    @Autowired private LPVSDetectService detectService;

    /**
     * Service for managing licenses and license conflicts.
     */
    @Autowired private LPVSLicenseService licenseService;

    /**
     * Repository for storing LPVSPullRequest entities.
     */
    @Autowired private LPVSPullRequestRepository lpvsPullRequestRepository;

    /**
     * Service for interacting with internal webhook.
     */
    @Autowired private LPVSQueueService queueService;

    /**
     * Repository for storing LPVSQueue entities.
     */
    @Autowired private LPVSQueueRepository queueRepository;

    /**
     * Maximum attempts for processing LPVSQueue elements.
     */
    @Value("${lpvs.attempts:4}")
    private int maxAttempts;

    /**
     * Creates a scan service based on the specified scanner type and configuration.
     *
     * @param isInternal  Flag indicating whether the LPVS mode of operation is internal or not.
     * @return An instance of {@link LPVSWebhookService} corresponding to the LPVS mode of operation.
     * @throws IllegalArgumentException if the specified scanner type is not supported or if an error occurs during
     *                                  the creation process.
     */
    public LPVSWebhookService createWebhookService(boolean isInternal) {
        try {
            Class<?> serviceClass =
                    Class.forName(
                            "com.lpvs."
                                    + (isInternal ? "internal." : "")
                                    + "service.webhook.LPVSWebhook"
                                    + (isInternal ? "Internal" : "")
                                    + "ServiceImpl");
            Constructor<?> constructor =
                    serviceClass.getDeclaredConstructor(
                            LPVSDetectService.class,
                            LPVSLicenseService.class,
                            LPVSGitHubService.class,
                            LPVSQueueService.class,
                            LPVSQueueRepository.class,
                            LPVSPullRequestRepository.class,
                            int.class);
            return (LPVSWebhookService)
                    constructor.newInstance(
                            detectService,
                            licenseService,
                            gitHubService,
                            queueService,
                            queueRepository,
                            lpvsPullRequestRepository,
                            maxAttempts);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error creating webhook service", e);
        }
    }
}
