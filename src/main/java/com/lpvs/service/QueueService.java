/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.config.WebhookConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class QueueService {

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private DetectService detectService;

    @Autowired
    private LicenseService licenseService;

    private static Logger LOG = LoggerFactory.getLogger(QueueService.class);

    private static final BlockingDeque<WebhookConfig> QUEUE =  new LinkedBlockingDeque<>();

    public void addFirst(WebhookConfig webhookConfig) throws InterruptedException {
        QUEUE.putFirst(webhookConfig);
    }

    public void delete(WebhookConfig webhookConfig) {
        QUEUE.remove(webhookConfig);
    }

    public WebhookConfig getQueueFirstElement() throws InterruptedException {
        return QUEUE.takeFirst();
    }

    @Async("threadPoolTaskExecutor")
    public void processWebHook(WebhookConfig webhookConfig) throws Exception {
        try {
                LOG.info(webhookConfig.toString());

                String filePath = gitHubService.getPullRequestFiles(webhookConfig);
                LOG.info("Successfully downloaded files from GitHub");

                if (filePath != null) {
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
                    gitHubService.commentResults(webhookConfig, files, detectedConflicts);
                    LOG.info("Results posted on GitHub");
                } else {
                    LOG.info("Files are not found. Probably pull request is not exists.");
                    gitHubService.commentResults(webhookConfig, new ArrayList<>(), new ArrayList<>());
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
