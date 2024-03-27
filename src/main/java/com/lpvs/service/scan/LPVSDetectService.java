/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.util.LPVSCommentUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for detecting licenses in GitHub pull requests using a specified scanner.
 */
@Service
@Slf4j
public class LPVSDetectService {

    /**
     * Service responsible for establishing and managing connections to the GitHub API.
     */
    private LPVSGitHubConnectionService gitHubConnectionService;

    /**
     * Service responsible for license conflict analysis.
     */
    private LPVSLicenseService licenseService;

    /**
     * Service responsible for GitHub connection and operation.
     */
    private LPVSGitHubService gitHubService;

    /**
     * Service responsible for initialization of the scanner.
     */
    private LPVSScanService scanService;

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
     * @param scannerType             The type of license detection scanner.
     * @param isInternal              Flag indicating whether the scanner is internal or not.
     * @param gitHubConnectionService Service for connecting to the GitHub API.
     * @param licenseService          Service for license conflict analysis.
     * @param gitHubService           Service for GitHub connection and operation.
     * @param scanServiceFactory      Service for creating instance of the scanner.
     */
    @Autowired
    public LPVSDetectService(
            @Value("${scanner:scanoss}") String scannerType,
            @Value("${internal:false}") boolean isInternal,
            LPVSGitHubConnectionService gitHubConnectionService,
            LPVSLicenseService licenseService,
            LPVSGitHubService gitHubService,
            LPVSScanServiceFactory scanServiceFactory) {
        this.gitHubConnectionService = gitHubConnectionService;
        this.licenseService = licenseService;
        this.gitHubService = gitHubService;
        this.scanService = scanServiceFactory.createScanService(scannerType, isInternal);
        log.info("License detection scanner: " + scannerType);
    }

    /**
     * Event listener method triggered when the application is ready, runs a single license scan if triggered.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runOneScan() {
        if (trigger != null && !HtmlUtils.htmlEscape(trigger).equals("")) {
            log.info("Triggered single scan operation");
            try {
                licenseService.reloadFromTables();
                LPVSQueue webhookConfig =
                        gitHubService.getInternalQueueByPullRequest(HtmlUtils.htmlEscape(trigger));

                List<LPVSFile> scanResult =
                        this.runScan(
                                webhookConfig, gitHubService.getPullRequestFiles(webhookConfig));

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
                log.info("Single scan completed.");
            } catch (Exception ex) {
                log.error("\n\n\n Single scan finished with errors \n\n\n");
                log.error("Can't trigger single scan: " + ex.getMessage());
            }
            SpringApplication.exit(ctx, () -> 0);
        }
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
        try {
            scanService.runScan(webhookConfig, path);
            return scanService.checkLicenses(webhookConfig);
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.error(ex.getMessage());
            return new ArrayList<>();
        }
    }
}
