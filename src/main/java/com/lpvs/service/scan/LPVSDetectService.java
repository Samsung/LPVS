/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lpvs.entity.report.LPVSReportBuilder;
import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.util.LPVSFileUtil;
import io.micrometer.common.util.StringUtils;
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

import static com.lpvs.entity.report.LPVSReportBuilder.saveHTMLToFile;

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
     * Component responsible for the generation of HTML reports.
     */
    private LPVSReportBuilder reportBuilder;

    /**
     * Trigger value to start a single scan of a pull request (optional).
     */
    @Value("${github.pull.request:}")
    private String trigger;

    /**
     * Trigger value to start a single scan of local files or folder (optional).
     */
    @Value("${local.path:}")
    private String localPath;

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
     * @param reportBuilder           Service for generating HTML reports.
     */
    @Autowired
    public LPVSDetectService(
            @Value("${scanner:scanoss}") String scannerType,
            @Value("${internal:false}") boolean isInternal,
            LPVSGitHubConnectionService gitHubConnectionService,
            LPVSLicenseService licenseService,
            LPVSGitHubService gitHubService,
            LPVSScanServiceFactory scanServiceFactory,
            LPVSReportBuilder reportBuilder) {
        this.gitHubConnectionService = gitHubConnectionService;
        this.licenseService = licenseService;
        this.gitHubService = gitHubService;
        this.scanService = scanServiceFactory.createScanService(scannerType, isInternal);
        log.info("License detection scanner: " + scannerType);
        this.reportBuilder = reportBuilder;
    }

    /**
     * Event listener method triggered when the application is ready, runs a single license scan if triggered.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runSingleScan() {
        // generateReport indicates that a report should be generated (HTML or command line output)
        boolean generateReport = false;
        LPVSQueue webhookConfig = null;
        List<LPVSFile> scanResult = null;
        List<LPVSLicenseService.Conflict<String, String>> detectedConflicts = null;
        String path = null;

        // Error case when both pull request scan and local files scan are set to true
        if (!StringUtils.isBlank(trigger) && !StringUtils.isBlank(localPath)) {
            log.error(
                    "Incorrect settings: both pull request scan and local files scan are set to true.");
            SpringApplication.exit(ctx, () -> 0);

            // Scan option - single pull request scan
        } else if (!StringUtils.isBlank(trigger)) {
            log.info("Triggered single scan of pull request.");
            try {
                licenseService.reloadFromTables();
                webhookConfig =
                        gitHubService.getInternalQueueByPullRequest(HtmlUtils.htmlEscape(trigger));

                scanResult =
                        this.runScan(
                                webhookConfig, gitHubService.getPullRequestFiles(webhookConfig));

                detectedConflicts = licenseService.findConflicts(webhookConfig, scanResult);
                generateReport = true;
                path = HtmlUtils.htmlEscape(trigger);
                log.info("Single scan of pull request completed.");
            } catch (Exception ex) {
                log.error("Single scan of pull request failed with error: " + ex.getMessage());
                SpringApplication.exit(ctx, () -> 0);
            }

            // Scan option - single scan of local file or folder
        } else if (!StringUtils.isBlank(localPath)) {
            log.info("Triggered single scan of local file(s).");
            try {
                licenseService.reloadFromTables();
                localPath = HtmlUtils.htmlEscape(localPath);
                File localFile = new File(localPath);
                if (localFile.exists()) {
                    // 1. Generate webhook config
                    webhookConfig = getInternalQueueByLocalPath();
                    // 2. Copy files
                    LPVSFileUtil.copyFiles(
                            localPath, LPVSFileUtil.getLocalDirectoryPath(webhookConfig));
                    // 3. Trigger scan
                    scanResult =
                            this.runScan(
                                    webhookConfig,
                                    LPVSFileUtil.getLocalDirectoryPath(webhookConfig));

                    detectedConflicts = licenseService.findConflicts(webhookConfig, scanResult);
                    generateReport = true;
                    path = localFile.getAbsolutePath();
                    log.info("Single scan of local file(s) completed.");
                } else {
                    throw new Exception("File path does not exist: " + localPath);
                }

            } catch (Exception ex) {
                log.error("Single scan of local file(s) failed with error: " + ex.getMessage());
                SpringApplication.exit(ctx, () -> 0);
            }
        }

        // Report generation
        // 1. HTML format
        if (generateReport && !StringUtils.isBlank(htmlReport)) {
            File report = new File(HtmlUtils.htmlEscape(htmlReport));
            String folderPath = report.getParent();
            if (folderPath == null) {
                folderPath = ".";
            }
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory()) {
                String reportFile =
                        reportBuilder.generateHtmlReportSingleScan(
                                path, scanResult, detectedConflicts, null, null);
                saveHTMLToFile(reportFile, report.getAbsolutePath());
            } else {
                log.error("Error: The parent directory '" + folder.getPath() + "' does not exist.");
            }
            SpringApplication.exit(ctx, () -> 0);
        } else if (generateReport) {
            // 2. Command line output
            String report =
                    LPVSCommentUtil.reportCommentBuilder(
                            webhookConfig, scanResult, detectedConflicts);
            if (!report.isEmpty()) {
                log.info(report);
            }
            SpringApplication.exit(ctx, () -> 0);
        }
    }

    /**
     * Creates a new LPVSQueue object with default values for a local scan.
     *
     * @return the new LPVSQueue object
     */
    private LPVSQueue getInternalQueueByLocalPath() {
        LPVSQueue queue = new LPVSQueue();
        queue.setDate(new Date());
        queue.setUserId("Single scan of local files run");
        queue.setReviewSystemType("local_scan");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String repoUrl = "local_scan_" + sdf.format(queue.getDate());
        queue.setRepositoryUrl(repoUrl);
        queue.setPullRequestUrl(repoUrl);
        return queue;
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
            List<LPVSFile> files = scanService.checkLicenses(webhookConfig);
            for (LPVSFile file : files) {
                if (file.getFilePath().startsWith(path)) {
                    file.setAbsoluteFilePath(file.getFilePath());
                    file.setFilePath(
                            file.getFilePath().substring(path.length()).replaceAll("\\\\", "/"));
                } else {
                    file.setFilePath(file.getFilePath().replaceAll("\\\\", "/"));
                    file.setAbsoluteFilePath(path + File.separator + file.getFilePath());
                }
                file.setMatchedLines(file.convertBytesToLinesNumbers());
            }
            return files;
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.error("Exception occurred during running the scan.");
            return new ArrayList<>();
        }
    }
}
