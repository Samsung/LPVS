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
import com.lpvs.util.LPVSCommentUtil;
import com.lpvs.util.LPVSFileUtil;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for detecting licenses in GitHub pull requests using a specified scanner.
 */
@Service
@Slf4j
public class LPVSDetectService {

    /**
     * The type of license detection scanner.
     */
    private String scannerType;

    /**
     * Service responsible for performing license detection using the ScanOSS scanner.
     */
    private LPVSScanossDetectService scanossDetectService;

    /**
     * Service responsible for establishing and managing connections to the GitHub API.
     */
    private LPVSGitHubConnectionService gitHubConnectionService;

    /**
     * Service responsible for license conflict analysis.
     */
    private LPVSLicenseService licenseService;

    /**
     * Event publisher for triggering application events.
     */
    @Autowired private ApplicationEventPublisher eventPublisher;

    /**
     * GitHub pull request used to trigger a single license scan (optional).
     */
    @Value("${github.pull.request:}")
    private String trigger;

    /**
     * Optional parameter to save html report to specified location.
     */
    @Value("${build.html.report:}")
    private String htmlReport;

    /**
     * Spring application context.
     */
    @Autowired ApplicationContext ctx;

    /**
     * Constructs an instance of LPVSDetectService with the specified parameters.
     *
     * @param scannerType            The type of license detection scanner.
     * @param gitHubConnectionService Service for connecting to the GitHub API.
     * @param scanossDetectService   Service for license detection using ScanOSS.
     */
    @Autowired
    public LPVSDetectService(
            @Value("${scanner:scanoss}") String scannerType,
            LPVSGitHubConnectionService gitHubConnectionService,
            LPVSScanossDetectService scanossDetectService,
            LPVSLicenseService licenseService) {
        this.scannerType = scannerType;
        this.gitHubConnectionService = gitHubConnectionService;
        this.scanossDetectService = scanossDetectService;
        this.licenseService = licenseService;
    }

    /**
     * Initializes the LPVSDetectService bean and logs the selected license detection scanner.
     */
    @PostConstruct
    private void init() {
        log.info("License detection scanner: " + scannerType);
    }

    /**
     * Event listener method triggered when the application is ready, runs a single license scan if triggered.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOneScan() {
        log.info("Triggered signle scan operation");
        if (trigger != null && !HtmlUtils.htmlEscape(trigger).equals("")) {
            try {
                LPVSQueue webhookConfig =
                        this.getInternalQueueByPullRequest(HtmlUtils.htmlEscape(trigger));

                List<LPVSFile> scanResult =
                        this.runScan(
                                webhookConfig,
                                LPVSDetectService.getPathByPullRequest(webhookConfig));

                List<LPVSLicenseService.Conflict<String, String>> detectedConflicts =
                        licenseService.findConflicts(webhookConfig, scanResult);

                if (htmlReport != null && !HtmlUtils.htmlEscape(htmlReport).equals("")) {
                    Path buildReportPath = Paths.get(htmlReport);
                    Path parentDirectory = buildReportPath.getParent();

                    if (parentDirectory != null && Files.isDirectory(parentDirectory)) {
                        String report =
                                LPVSCommentUtil.buildHTMLComment(
                                        webhookConfig, scanResult, detectedConflicts);
                        LPVSCommentUtil.saveHTMLToFile(report, buildReportPath.toString());
                    } else {
                        log.error(
                                "Error: The parent directory '"
                                        + parentDirectory
                                        + "' does not exist.");
                    }
                } else {
                    String report =
                            LPVSCommentUtil.reportCommentBuilder(
                                    webhookConfig, scanResult, detectedConflicts);
                    log.info(report);
                }
            } catch (Exception ex) {
                log.error("\n\n\n Single scan finished with errors \n\n\n");
                log.error("Can't triger single scan " + ex);
            }
            // exiting application
            eventPublisher.publishEvent(new ExitCodeEvent(new Object(), 0));
        }
    }

    /**
     * Retrieves an LPVSQueue configuration based on the GitHub repository and pull request.
     *
     * @param repo The GitHub repository.
     * @param pR   The GitHub pull request.
     * @return LPVSQueue configuration for the given GitHub repository and pull request.
     */
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

    /**
     * Retrieves the LPVSQueue configuration for a given GitHub pull request URL.
     *
     * @param pullRequest The GitHub pull request URL.
     * @return LPVSQueue configuration for the given pull request.
     */
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

    /**
     * Retrieves the local directory path for a given LPVSQueue configuration.
     *
     * @param webhookConfig LPVSQueue configuration.
     * @return Local directory path for the given LPVSQueue.
     */
    public static String getPathByPullRequest(LPVSQueue webhookConfig) {
        if (webhookConfig == null) return null;
        return LPVSFileUtil.getLocalDirectoryPath(webhookConfig);
    }

    /**
     * Runs a license scan based on the selected scanner type.
     *
     * @param webhookConfig LPVSQueue configuration for the scan.
     * @param path          Local directory path for the scan.
     * @return List of LPVSFile objects representing the scan results.
     * @throws Exception if an error occurs during the scan.
     */
    public List<LPVSFile> runScan(LPVSQueue webhookConfig, String path) throws Exception {
        if (scannerType.equals("scanoss")) {
            scanossDetectService.runScan(webhookConfig, path);
            return scanossDetectService.checkLicenses(webhookConfig);
        }
        return new ArrayList<>();
    }
}
