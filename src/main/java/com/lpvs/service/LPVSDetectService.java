/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.service.scanner.scanoss.LPVSScanossDetectService;
import com.lpvs.util.LPVSFileUtil;
import com.nimbusds.jose.util.IOUtils;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ExitCodeEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class LPVSDetectService {

    private String scannerType;

    private LPVSScanossDetectService scanossDetectService;

    private LPVSGitHubConnectionService gitHubConnectionService;

    @Autowired private ApplicationEventPublisher eventPublisher;

    @Value("${github.pull.request:}")
    private String trigger;

    @Autowired ApplicationContext ctx;

    @Autowired
    public LPVSDetectService(
            @Value("${scanner:scanoss}") String scannerType,
            LPVSGitHubConnectionService gitHubConnectionService,
            LPVSScanossDetectService scanossDetectService) {
        this.scannerType = scannerType;
        this.gitHubConnectionService = gitHubConnectionService;
        this.scanossDetectService = scanossDetectService;
    }

    @PostConstruct
    private void init() {
        log.info("License detection scanner: " + scannerType);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOneScan() {
        if (trigger != null && !HtmlUtils.htmlEscape(trigger).equals("")) {
            try {
                LPVSQueue webhookConfig = this.getInternalQueueByPullRequest(HtmlUtils.htmlEscape(trigger));
                this.runScan(webhookConfig, LPVSDetectService.getPathByPullRequest(webhookConfig));
                File scanResult = new File(LPVSFileUtil.getScanResultsJsonFilePath(webhookConfig));
                if (scanResult.exists()) {
                    String jsonTxt = IOUtils.readFileToString(scanResult);
                    // ToDo: form html report and console output
                    log.info(jsonTxt);
                    log.info("\n\n\n Single scan finished successfully \n\n\n");
                }
            } catch (Exception ex) {
                log.info("\n\n\n Single scan finished with errors \n\n\n");
                log.error("Can't triger single scan " + ex);
            }
            // exiting application
            eventPublisher.publishEvent(new ExitCodeEvent(new Object(), 0));
        }
    }

    private static LPVSQueue getGitHubWebhookConfig(GHRepository repo, GHPullRequest pR) {
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setPullRequestUrl(
                pR.getHtmlUrl() != null ? pR.getHtmlUrl().toString() : null);
        if (pR.getHead() != null
                && pR.getHead().getRepository() != null
                && pR.getHead().getRepository().getHtmlUrl() != null) {
            webhookConfig.setPullRequestFilesUrl(
                    pR.getHead().getRepository().getHtmlUrl().toString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(pR.getUrl() != null ? pR.getUrl().toString() : null);
        webhookConfig.setRepositoryUrl(
                repo.getHtmlUrl() != null ? repo.getHtmlUrl().toString() : null);
        webhookConfig.setUserId("Single scan run");
        webhookConfig.setHeadCommitSHA(pR.getHead() != null ? pR.getHead().getSha() : null);
        return webhookConfig;
    }

    public LPVSQueue getInternalQueueByPullRequest(String pullRequest) {
        try {
            if (pullRequest == null) return null;
            String[] pullRequestSplit = pullRequest.split("/");
            if (pullRequestSplit.length < 5) return null;
            String pullRequestRepo =
                    String.join(
                            "/",
                            Arrays.asList(pullRequestSplit)
                                    .subList(
                                            pullRequestSplit.length - 4,
                                            pullRequestSplit.length - 2));
            int pullRequestNum = Integer.parseInt(pullRequestSplit[pullRequestSplit.length - 1]);
            GitHub gitHub = gitHubConnectionService.connectToGitHubApi();
            GHRepository repo = gitHub.getRepository(pullRequestRepo);
            GHPullRequest pR = repo.getPullRequest(pullRequestNum);
            return LPVSDetectService.getGitHubWebhookConfig(repo, pR);
        } catch (IOException e) {
            log.error("Can't set up github client: " + e);
        }
        return null;
    }

    public static String getPathByPullRequest(LPVSQueue webhookConfig) {
        if (webhookConfig == null) return null;
        return LPVSFileUtil.getLocalDirectoryPath(webhookConfig);
    }

    public List<LPVSFile> runScan(LPVSQueue webhookConfig, String path) throws Exception {
        if (scannerType.equals("scanoss")) {
            scanossDetectService.runScan(webhookConfig, path);
            return scanossDetectService.checkLicenses(webhookConfig);
        }
        return new ArrayList<>();
    }
}
