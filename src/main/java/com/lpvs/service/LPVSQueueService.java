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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
@Slf4j
public class LPVSQueueService {

    private LPVSGitHubService gitHubService;

    private LPVSDetectService detectService;

    private LPVSLicenseService licenseService;

    private LPVSPullRequestRepository lpvsPullRequestRepository;

    private LPVSQueueRepository queueRepository;

    private int maxAttempts;

    @Value("${soshub.url}")
    private String SOSHUB_URL;

    @Autowired
    public LPVSQueueService(LPVSGitHubService gitHubService,
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

    public LPVSQueue getQueueFirstElement() throws InterruptedException {
        return QUEUE.takeFirst();
    }

    private static final BlockingDeque<LPVSQueue> QUEUE =  new LinkedBlockingDeque<>();

    public void addFirst(LPVSQueue webhookConfig) throws InterruptedException {
        QUEUE.putFirst(webhookConfig);
    }

    public void add(LPVSQueue webhookConfig) throws InterruptedException {
        QUEUE.put(webhookConfig);
    }

    public void delete(LPVSQueue webhookConfig) {
        queueRepository.deleteById(webhookConfig.getId());
        QUEUE.remove(webhookConfig);
    }

    public BlockingDeque<LPVSQueue> getQueue() {
        return QUEUE;
    }

    public void checkForQueue() throws InterruptedException {
        log.debug("Checking for previous queue");
        List<LPVSQueue> webhookConfigList = queueRepository.getQueueList();
        if (webhookConfigList.size() > 0) {
            LPVSQueue webhookConfig = getLatestScan(webhookConfigList);
            webhookConfig.setAttempts(webhookConfig.getAttempts() + 1);
            queueRepository.save(webhookConfig);
        }
        for (LPVSQueue webhook: webhookConfigList){
            log.info("PROCESSING WebHook id = " + webhook.getId());
            if (webhook.getAttempts() > maxAttempts) {
                LPVSPullRequest pullRequest = new LPVSPullRequest();
                pullRequest.setPullRequestUrl(webhook.getPullRequestUrl());
                pullRequest.setUser(webhook.getUserId());
                pullRequest.setPullRequestFilesUrl(webhook.getPullRequestFilesUrl());
                pullRequest.setRepositoryName(LPVSWebhookUtil.getRepositoryOrganization(webhook) + "/" + LPVSWebhookUtil.getRepositoryName(webhook));
                pullRequest.setDate(webhook.getDate());
                pullRequest.setStatus(LPVSPullRequestStatus.NO_ACCESS.toString());
                pullRequest = lpvsPullRequestRepository.saveAndFlush(pullRequest);

                if (webhook.getUserId().equals("GitHub hook")) {
                    gitHubService.setErrorCheck(webhook);
                }
                queueRepository.deleteById(webhook.getId());
                log.info("Webhook id = " + webhook.getId() + " is deleted. Details on PR #" + pullRequest.getId());
                continue;
            }
            log.info("Add WebHook id = " + webhook.getId() + " to the queue.");
            QUEUE.putFirst(webhook);
        }
    }

    private LPVSQueue getLatestScan(List<LPVSQueue> webhookConfigList) {
        LPVSQueue latestWebhookConfig = webhookConfigList.get(0);
        for (LPVSQueue webhookConfig: webhookConfigList) {
            if(latestWebhookConfig.getDate().compareTo(webhookConfig.getDate()) < 0) {
                latestWebhookConfig = webhookConfig;
            }
        }
        return latestWebhookConfig;
    }

    @Async("threadPoolTaskExecutor")
    public void processWebHook(LPVSQueue webhookConfig) throws IOException {
        LPVSPullRequest pullRequest = new LPVSPullRequest();
        try {
                log.info("GitHub Webhook processing...");
                log.info(webhookConfig.toString());

                String filePath = gitHubService.getPullRequestFiles(webhookConfig);

                pullRequest.setPullRequestUrl(webhookConfig.getPullRequestUrl());
                pullRequest.setUser(webhookConfig.getUserId());
                pullRequest.setPullRequestFilesUrl(webhookConfig.getPullRequestFilesUrl());
                pullRequest.setRepositoryName(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig) + "/" + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                pullRequest.setDate(webhookConfig.getDate());
                pullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
                pullRequest = lpvsPullRequestRepository.saveAndFlush(pullRequest);
                log.debug("ID: " + pullRequest.getId() + " " + pullRequest.toString());

                if (filePath != null) {
                    log.debug("Successfully downloaded files");

                    if (filePath.contains(":::::")) {
                        filePath = filePath.split(":::::")[0];
                    }
                    // check repository license
                    String repositoryLicense = gitHubService.getRepositoryLicense(webhookConfig);
                    if (licenseService.checkLicense(repositoryLicense) != null) {
                        webhookConfig.setRepositoryLicense(licenseService.checkLicense(repositoryLicense).getSpdxId());
                    } else if (licenseService.findLicenseByName(repositoryLicense) != null) {
                        webhookConfig.setRepositoryLicense(licenseService.findLicenseByName(repositoryLicense).getSpdxId());
                    } else {
                        webhookConfig.setRepositoryLicense(null);
                    }
                    log.debug("Repository license: " + webhookConfig.getRepositoryLicense());

                    List<LPVSFile> files = detectService.runScan(webhookConfig, filePath);

                    // check license conflicts
                    List<LPVSLicenseService.Conflict<String, String>> detectedConflicts = licenseService.findConflicts(webhookConfig, files);

                    log.debug("Creating comment");
                    webhookConfig.setHubLink(SOSHUB_URL + "/view/lpvs/" + pullRequest.getId());
                    gitHubService.commentResults(webhookConfig, files, detectedConflicts, pullRequest);
                    log.debug("Results posted on GitHub");
                } else {
                    log.warn("Files are not found. Probably pull request is not exists.");
                    webhookConfig.setHubLink(SOSHUB_URL + "/view/lpvs/" + pullRequest.getId());
                    gitHubService.commentResults(webhookConfig, null, null, pullRequest);
                    delete(webhookConfig);
                    throw new Exception("Files are not found. Probably pull request is not exists. Terminating.");
                }
                delete(webhookConfig);
        } catch (Exception | Error e) {
            pullRequest.setStatus(LPVSPullRequestStatus.INTERNAL_ERROR.toString());
            pullRequest = lpvsPullRequestRepository.saveAndFlush(pullRequest);
            log.error("Can't authorize commentResults() " + e);
            e.printStackTrace();
            webhookConfig.setHubLink(SOSHUB_URL + "/view/lpvs/" + pullRequest.getId());
            delete(webhookConfig);
        }
    }

}
