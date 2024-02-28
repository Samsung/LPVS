/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestStatus;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.util.LPVSWebhookUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Service for managing LPVSQueue elements and processing webhooks.
 */
@Service
@Slf4j
public class LPVSQueueService {

    /**
     * Service for interacting with GitHub.
     */
    private LPVSGitHubService gitHubService;

    /**
     * Service for detecting licenses in scanned files.
     */
    private LPVSDetectService detectService;

    /**
     * Service for managing licenses and license conflicts.
     */
    private LPVSLicenseService licenseService;

    /**
     * Repository for storing LPVSPullRequest entities.
     */
    private LPVSPullRequestRepository lpvsPullRequestRepository;

    /**
     * Repository for storing LPVSQueue entities.
     */
    private LPVSQueueRepository queueRepository;

    /**
     * Maximum attempts for processing LPVSQueue elements.
     */
    private int maxAttempts;

    /**
     * BlockingDeque for managing LPVSQueue elements.
     */
    private static final BlockingDeque<LPVSQueue> QUEUE = new LinkedBlockingDeque<>();

    /**
     * Constructor for LPVSQueueService.
     *
     * @param gitHubService             Service for interacting with GitHub.
     * @param detectService             Service for detecting licenses in scanned files.
     * @param licenseService            Service for managing licenses and license conflicts.
     * @param lpvsPullRequestRepository Repository for storing LPVSPullRequest entities.
     * @param queueRepository           Repository for storing LPVSQueue entities.
     * @param maxAttempts               Maximum attempts for processing LPVSQueue elements.
     */
    public LPVSQueueService(
            LPVSGitHubService gitHubService,
            LPVSDetectService detectService,
            LPVSLicenseService licenseService,
            LPVSPullRequestRepository lpvsPullRequestRepository,
            LPVSQueueRepository queueRepository,
            @Value("${lpvs.attempts:4}") int maxAttempts) {
        this.gitHubService = gitHubService;
        this.detectService = detectService;
        this.licenseService = licenseService;
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.queueRepository = queueRepository;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Gets the first element from the LPVSQueue.
     *
     * @return The first LPVSQueue element.
     * @throws InterruptedException If interrupted while waiting for the element.
     */
    public LPVSQueue getQueueFirstElement() throws InterruptedException {
        return QUEUE.takeFirst();
    }

    /**
     * Adds the LPVSQueue element to the front of the queue.
     *
     * @param webhookConfig The LPVSQueue element to be added.
     * @throws InterruptedException If interrupted while waiting to add the element.
     */
    public void addFirst(LPVSQueue webhookConfig) throws InterruptedException {
        QUEUE.putFirst(webhookConfig);
    }

    /**
     * Adds the LPVSQueue element to the end of the queue.
     *
     * @param webhookConfig The LPVSQueue element to be added.
     * @throws InterruptedException If interrupted while waiting to add the element.
     */
    public void add(LPVSQueue webhookConfig) throws InterruptedException {
        QUEUE.put(webhookConfig);
    }

    /**
     * Deletes the LPVSQueue element from the repository and the queue.
     *
     * @param webhookConfig The LPVSQueue element to be deleted.
     */
    public void delete(LPVSQueue webhookConfig) {
        queueRepository.deleteById(webhookConfig.getId());
        QUEUE.remove(webhookConfig);
    }

    /**
     * Gets the entire LPVSQueue.
     *
     * @return The BlockingDeque containing LPVSQueue elements.
     */
    public BlockingDeque<LPVSQueue> getQueue() {
        return QUEUE;
    }

    /**
     * Checks for any previous LPVSQueue elements and processes them.
     *
     * @throws InterruptedException If interrupted while processing the queue.
     */
    public void checkForQueue() throws InterruptedException {
        QUEUE.clear();
        log.debug("Checking for previous queue");
        List<LPVSQueue> webhookConfigList = queueRepository.getQueueList();
        for (LPVSQueue webhook : webhookConfigList) {
            log.info("Add WebHook id = " + webhook.getId() + " to the queue.");
            QUEUE.putFirst(webhook);
        }
    }

    /**
     * Gets the LPVSQueue element with the latest scan date.
     *
     * @param webhookConfigList The list of LPVSQueue elements.
     * @return The LPVSQueue element with the latest scan date.
     */
    public LPVSQueue getLatestScan(List<LPVSQueue> webhookConfigList) {
        LPVSQueue latestWebhookConfig = webhookConfigList.get(0);
        for (LPVSQueue webhookConfig : webhookConfigList) {
            if (latestWebhookConfig.getDate().compareTo(webhookConfig.getDate()) < 0) {
                latestWebhookConfig = webhookConfig;
            }
        }
        return latestWebhookConfig;
    }

    /**
     * Asynchronously processes the LPVSQueue element, handling GitHub webhook events.
     *
     * @param webhookConfig The LPVSQueue element to be processed.
     */
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
        LPVSPullRequest pullRequest =
                lpvsPullRequestRepository.findLatestByPullRequestInfo(
                        webhookConfig.getUserId(),
                        LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                + "/"
                                + LPVSWebhookUtil.getRepositoryName(webhookConfig),
                        webhookConfig.getPullRequestFilesUrl(),
                        webhookConfig.getPullRequestHead(),
                        webhookConfig.getPullRequestBase(),
                        webhookConfig.getSender(),
                        LPVSPullRequestStatus.INTERNAL_ERROR.getPullRequestStatus());

        if (pullRequest == null) {
            pullRequest = new LPVSPullRequest();
            pullRequest.setUser(webhookConfig.getUserId());
            pullRequest.setRepositoryName(
                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                            + "/"
                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
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

                if (filePath.contains(":::::")) {
                    filePath = filePath.split(":::::")[0];
                }
                // check repository license
                String repositoryLicense = gitHubService.getRepositoryLicense(webhookConfig);
                if (licenseService.checkLicense(repositoryLicense) != null) {
                    webhookConfig.setRepositoryLicense(
                            licenseService.checkLicense(repositoryLicense).getSpdxId());
                } else if (licenseService.findLicenseByName(repositoryLicense) != null) {
                    webhookConfig.setRepositoryLicense(
                            licenseService.findLicenseByName(repositoryLicense).getSpdxId());
                } else {
                    webhookConfig.setRepositoryLicense(null);
                }
                log.debug("Repository license: " + webhookConfig.getRepositoryLicense());

                List<LPVSFile> files = detectService.runScan(webhookConfig, filePath);

                // check license conflicts
                List<LPVSLicenseService.Conflict<String, String>> detectedConflicts =
                        licenseService.findConflicts(webhookConfig, files);

                log.debug("Creating comment");
                gitHubService.commentResults(webhookConfig, files, detectedConflicts, pullRequest);
                log.debug("Results posted on GitHub");
                delete(webhookConfig);
            } else {
                log.warn("Files are not found. Probably pull request is not exists.");
                throw new Exception(
                        "Files are not found. Probably pull request does not exist. Terminating.");
            }
        } catch (Exception | Error e) {
            pullRequest.setStatus(LPVSPullRequestStatus.INTERNAL_ERROR.toString());
            pullRequest = lpvsPullRequestRepository.saveAndFlush(pullRequest);
            log.error("Can't authorize commentResults() " + e.getMessage());
            int currentAttempts = webhookConfig.getAttempts() + 1;
            if (currentAttempts < maxAttempts) {
                webhookConfig.setAttempts(currentAttempts);
                try {
                    addFirst(webhookConfig);
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
                } catch (IOException e1) {
                    log.warn("Failed to post FAIL result " + e.getMessage());
                }
                delete(webhookConfig);
                log.info(
                        "Webhook ID: "
                                + id
                                + " - removed from the queue because the number of attempts exceeded the max value");
            }
        }
    }
}
