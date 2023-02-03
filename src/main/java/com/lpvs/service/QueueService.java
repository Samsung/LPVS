/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.entity.enums.PullRequestStatus;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.repository.QueueRepository;
import com.lpvs.util.WebhookUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class QueueService {

    private GitHubService gitHubService;

    private DetectService detectService;

    private LicenseService licenseService;

    private int maxAttempts;

    @Autowired
    public QueueService(GitHubService gitHubService,
                        DetectService detectService,
                        LicenseService licenseService,
                        @Value("${lpvs.attempts:4}") int maxAttempts) {
        this.gitHubService = gitHubService;
        this.detectService = detectService;
        this.licenseService = licenseService;
        this.maxAttempts = maxAttempts;
    }

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private LPVSPullRequestRepository pullRequestRepository;

    private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);

    private static final BlockingDeque<WebhookConfig> QUEUE =  new LinkedBlockingDeque<>();

    public void addFirst(WebhookConfig webhookConfig) throws InterruptedException {
        QUEUE.putFirst(webhookConfig);
    }

    public void delete(WebhookConfig webhookConfig) {
        QUEUE.remove(webhookConfig);
    }

    public BlockingDeque<WebhookConfig> getQueue() {
        return QUEUE;
    }

    public void checkForQueue() throws InterruptedException {
        LOG.info("Checking for previous queue");
        List<WebhookConfig> webhookConfigList = queueRepository.getQueueList();
        if (webhookConfigList.size() > 0) {
            WebhookConfig webhookConfig = getLatestScan(webhookConfigList);
            webhookConfig.setAttempts(webhookConfig.getAttempts() + 1);
            queueRepository.save(webhookConfig);
        }
        for (WebhookConfig webhook: webhookConfigList){
            LOG.info("PROCESSING WebHook id = " + webhook.getId());
            if (webhook.getAttempts() > maxAttempts) {
                LPVSPullRequest pullRequest = new LPVSPullRequest();
                pullRequest.setPullRequestUrl(webhook.getPullRequestUrl());
                pullRequest.setPullRequestFilesUrl(webhook.getPullRequestFilesUrl());
                pullRequest.setRepositoryName(WebhookUtil.getRepositoryOrganization(webhook) + "/" + WebhookUtil.getRepositoryName(webhook));
                pullRequest.setDate(webhook.getDate());
                pullRequest.setStatus(PullRequestStatus.NO_ACCESS.toString());
                pullRequestRepository.save(pullRequest);

                if (webhook.getUserId().equals("bot")) {
                    gitHubService.setErrorCheck(webhook);
                }

                queueRepository.deleteById(webhook.getId());
                continue;
            }
            LOG.info("Add WebHook id = " + webhook.getId());
            QUEUE.putFirst(webhook);
        }
    }

    private WebhookConfig getLatestScan(List<WebhookConfig> webhookConfigList) {
        WebhookConfig latestWebhookConfig = webhookConfigList.get(0);
        for (WebhookConfig webhookConfig: webhookConfigList) {
            if(latestWebhookConfig.getDate().compareTo(webhookConfig.getDate()) < 0) {
                latestWebhookConfig = webhookConfig;
            }
        }
        return latestWebhookConfig;
    }

    @Async("threadPoolTaskExecutor")
    public void processWebHook(WebhookConfig webhookConfig) {
        try {
                LOG.info(webhookConfig.toString());

                String filePath = gitHubService.getPullRequestFiles(webhookConfig);

                LPVSPullRequest pullRequest = new LPVSPullRequest();
                pullRequest.setPullRequestUrl(webhookConfig.getPullRequestUrl());
                pullRequest.setPullRequestFilesUrl(webhookConfig.getPullRequestFilesUrl());
                pullRequest.setRepositoryName(WebhookUtil.getRepositoryOrganization(webhookConfig) + "/" + WebhookUtil.getRepositoryName(webhookConfig));
                pullRequest.setDate(webhookConfig.getDate());

                if (filePath != null) {
                    LOG.info("Successfully downloaded files");

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
                    LOG.info("Repository license: " + webhookConfig.getRepositoryLicense());

                    List<LPVSFile> files = detectService.runScan(webhookConfig, filePath);

                    // check license conflicts
                    List<LicenseService.Conflict<String, String>> detectedConflicts = licenseService.findConflicts(webhookConfig, files);

                    LOG.info("Creating comment");
                    gitHubService.commentResults(webhookConfig, files, detectedConflicts, pullRequest);
                    LOG.info("Results posted on GitHub");
                } else {
                    LOG.info("Files are not found. Probably pull request is not exists.");
                    gitHubService.commentResults(webhookConfig, new ArrayList<>(), new ArrayList<>(), pullRequest);
                    delete(webhookConfig);
                    throw new Exception("Files are not found. Probably pull request is not exists. Terminating.");
                }
            delete(webhookConfig);
        } catch (Exception | Error e) {
            LOG.error(e.toString());
            gitHubService.setErrorCheck(webhookConfig);
            delete(webhookConfig);
        }
    }

}
