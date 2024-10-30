/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.webhook;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestStatus;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.entity.LPVSConflict;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.LPVSQueueService;
import com.lpvs.service.scan.LPVSDetectService;
import com.lpvs.util.LPVSPayloadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * The LPVSWebhookServiceImpl class provides an implementation of the LPVSWebhookService interface.
 */
@Slf4j
@Service
public class LPVSWebhookServiceImpl implements LPVSWebhookService {

    /**
     * Repository for storing LPVSPullRequest entities.
     */
    private final LPVSPullRequestRepository lpvsPullRequestRepository;

    /**
     * Service for detecting licenses in scanned files.
     */
    private final LPVSDetectService detectService;

    /**
     * Service for managing licenses and license conflicts.
     */
    private final LPVSLicenseService licenseService;

    /**
     * Repository for storing LPVSQueue entities.
     */
    private final LPVSQueueRepository queueRepository;

    /**
     * Service for interacting with GitHub.
     */
    private final LPVSGitHubService gitHubService;

    /**
     * Service for interacting with internal webhook.
     */
    private final LPVSQueueService queueService;

    /**
     * Maximum attempts for processing LPVSQueue elements.
     */
    private final int maxAttempts;

    /**
     * Constructor for LPVSWebhookServiceImpl.
     *
     * @param detectService Service for detecting licenses in scanned files.
     * @param licenseService Service for managing licenses and license conflicts.
     * @param gitHubService Service for interacting with GitHub.
     * @param queueService Service for interacting with the queue.
     * @param queueRepository Repository for storing LPVSQueue entities.
     * @param lpvsPullRequestRepository Repository for storing LPVSPullRequest entities.
     * @param maxAttempts Maximum attempts for processing LPVSQueue elements.
     */
    public LPVSWebhookServiceImpl(
            LPVSDetectService detectService,
            LPVSLicenseService licenseService,
            LPVSGitHubService gitHubService,
            LPVSQueueService queueService,
            LPVSQueueRepository queueRepository,
            LPVSPullRequestRepository lpvsPullRequestRepository,
            @Value("${lpvs.attempts:4}") int maxAttempts) {
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.detectService = detectService;
        this.licenseService = licenseService;
        this.queueRepository = queueRepository;
        this.gitHubService = gitHubService;
        this.queueService = queueService;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Asynchronously processes the LPVSQueue element, handling GitHub webhook events.
     *
     * @param webhookConfig The LPVSQueue element to be processed.
     */
    @Override
    @Async("threadPoolTaskExecutor")
    public void processWebHook(LPVSQueue webhookConfig) {
        Long id = webhookConfig.getId();
        log.info(
                "Processing webhook ID: "
                        + id
                        + ", attempt: "
                        + (webhookConfig.getAttempts() + 1)
                        + " for PR: "
                        + webhookConfig.getPullRequestUrl());
        LPVSPullRequest pullRequest = lpvsPullRequestRepository.findByQueueId(id);

        if (pullRequest == null) {
            pullRequest = new LPVSPullRequest();
            pullRequest.setQueueId(id);
            pullRequest.setUser(webhookConfig.getUserId());
            pullRequest.setRepositoryName(
                    LPVSPayloadUtil.getRepositoryOrganization(webhookConfig)
                            + "/"
                            + LPVSPayloadUtil.getRepositoryName(webhookConfig));
            pullRequest.setPullRequestUrl(webhookConfig.getPullRequestUrl());
            pullRequest.setPullRequestFilesUrl(webhookConfig.getPullRequestFilesUrl());
            pullRequest.setPullRequestHead(webhookConfig.getPullRequestHead());
            pullRequest.setPullRequestBase(webhookConfig.getPullRequestBase());
            pullRequest.setSender(webhookConfig.getSender());
        }

        try {

            pullRequest.setDate(webhookConfig.getDate());
            pullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            pullRequest = lpvsPullRequestRepository.saveAndFlush(pullRequest);

            String filePath = gitHubService.getPullRequestFiles(webhookConfig);
            if (filePath != null && Files.list(Paths.get(filePath)).count() != 0) {
                log.debug("Successfully downloaded files");

                // check repository license
                String[] repositoryLicense = gitHubService.getRepositoryLicense(webhookConfig);

                if (repositoryLicense != null) {
                    LPVSLicense repoLicense =
                            licenseService.getLicenseBySpdxIdAndName(
                                    repositoryLicense[0],
                                    Optional.ofNullable(repositoryLicense[1]));
                    webhookConfig.setRepositoryLicense(repoLicense.getSpdxId());
                } else {
                    webhookConfig.setRepositoryLicense(null);
                }
                log.debug("Repository license: " + webhookConfig.getRepositoryLicense());

                List<LPVSFile> files = detectService.runScan(webhookConfig, filePath);

                // check license conflicts
                List<LPVSConflict<String, String>> detectedConflicts =
                        licenseService.findConflicts(webhookConfig, files);

                log.debug("Creating comment");
                gitHubService.commentResults(webhookConfig, files, detectedConflicts, pullRequest);
                log.debug("Results posted on GitHub");
                queueService.delete(webhookConfig);
            } else {
                log.warn("Files are not found. Probably pull request does not exist.");
                throw new Exception(
                        "Files are not found. Probably pull request does not exist. Terminating.");
            }
            log.info("Webhook ID: " + id + " - processing successfully completed");
        } catch (Exception | Error e) {
            pullRequest.setStatus(LPVSPullRequestStatus.INTERNAL_ERROR.toString());
            pullRequest = lpvsPullRequestRepository.saveAndFlush(pullRequest);
            log.error("Can't authorize commentResults() " + e.getMessage());
            int currentAttempts = webhookConfig.getAttempts() + 1;
            if (currentAttempts < maxAttempts) {
                webhookConfig.setAttempts(currentAttempts);
                try {
                    queueService.addFirst(webhookConfig);
                } catch (InterruptedException e1) {
                    log.warn("Failed to update Queue element");
                }
                queueRepository.save(webhookConfig);
            } else {
                log.warn(
                        "Maximum amount of processing webhook reached for pull request: "
                                + pullRequest.getId()
                                + " "
                                + pullRequest.getPullRequestUrl());
                try {
                    gitHubService.commentResults(webhookConfig, null, null, pullRequest);
                } catch (Exception ex) {
                    log.error("Exception occurred: " + ex.getMessage());
                }
                queueService.delete(webhookConfig);
                log.info(
                        "Webhook ID: "
                                + id
                                + " - removed from the webhook because the number of attempts exceeded the max value");
            }
        }
    }
}
