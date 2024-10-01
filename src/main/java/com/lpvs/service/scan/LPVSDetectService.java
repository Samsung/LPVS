/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import com.lpvs.entity.report.LPVSReportBuilder;
import com.lpvs.entity.LPVSConflict;
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
     * List of services responsible for initialization of the scanners.
     */
    private List<LPVSScanService> scanServiceList = new ArrayList<>();

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

    /** TODO change description
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
            @Value("${internal:false}") String isInternal,
            LPVSGitHubConnectionService gitHubConnectionService,
            LPVSLicenseService licenseService,
            LPVSGitHubService gitHubService,
            LPVSScanServiceFactory scanServiceFactory,
            LPVSReportBuilder reportBuilder) {
        this.gitHubConnectionService = gitHubConnectionService;
        this.licenseService = licenseService;
        this.gitHubService = gitHubService;
        this.reportBuilder = reportBuilder;

        List<String> scannerTypeList =
                Arrays.stream(scannerType.split(",")).map(String::trim).toList();
        List<Boolean> isInternalList =
                Arrays.stream(isInternal.split(","))
                        .map(String::trim)
                        .map(Boolean::parseBoolean)
                        .toList();
        if (scannerTypeList.size() != isInternalList.size()) {
            log.error(
                    "The number of declared scanners and internal flags do not match. The default values will be used.");
            scannerTypeList = Collections.singletonList("scanoss");
            isInternalList = Collections.singletonList(false);
        }
        for (int ind = 0; ind < scannerTypeList.size(); ind++) {
            scanServiceList.add(
                    scanServiceFactory.createScanService(
                            scannerTypeList.get(ind), isInternalList.get(ind)));
        }
        log.info("License detection scanner(s): " + String.join(", ", scannerTypeList));
    }

    /**
     * Event listener method triggered when the application is ready, runs a single license scan if triggered.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runSingleScan() {
        // generateReport indicates that a report should be generated (HTML or command line output)
        boolean generateReport = false;
        LPVSQueue webhookConfig;
        List<LPVSFile> scanResult = new ArrayList<>();
        List<LPVSConflict<String, String>> detectedConflicts = null;
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
                    reportBuilder.generateCommandLineComment(path, scanResult, detectedConflicts);
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

    public List<LPVSFile> runScan(LPVSQueue webhookConfig, String path) throws Exception {
        List<LPVSFile> scanResult = new ArrayList<>();
        for (LPVSScanService lpvsScanService : scanServiceList) {
            List<LPVSFile> scanResultForScanner =
                    this.runScan(webhookConfig, path, lpvsScanService);
            // Merge scan results
            scanResult = mergeScanResults(scanResult, scanResultForScanner);
        }
        return scanResult;
    }

    /** TODO add scan service
     * Runs a license scan based on the selected scanner type.
     *
     * @param webhookConfig LPVSQueue configuration for the scan.
     * @param path          Local directory path for the scan.
     * @return List of LPVSFile objects representing the scan results.
     * @throws Exception if an error occurs during the scan.
     */
    private List<LPVSFile> runScan(
            LPVSQueue webhookConfig, String path, LPVSScanService scanService) throws Exception {
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

    // TODO add comment
    private List<LPVSFile> mergeScanResults(
            List<LPVSFile> resultMergeTo, List<LPVSFile> resultMergeWith) {
        for (LPVSFile file : resultMergeWith) {
            boolean isUpdated = false;
            for (LPVSFile finalFile : resultMergeTo) {
                // if component is the same - just merge license information
                if (finalFile
                                .getFilePath()
                                .replaceAll("/", "")
                                .equalsIgnoreCase(file.getFilePath().replaceAll("/", ""))
                        && finalFile.getComponentName().equalsIgnoreCase(file.getComponentName())
                        && finalFile
                                .getComponentVendor()
                                .equalsIgnoreCase(file.getComponentVendor())
                        && finalFile
                                .getComponentVersion()
                                .equalsIgnoreCase(file.getComponentVersion())) {
                    if (finalFile.getFilePath().startsWith("/")) {
                        finalFile.setFilePath(finalFile.getFilePath().substring(1));
                    }
                    finalFile.getLicenses().addAll(file.getLicenses());
                    finalFile.setLicenses(new HashSet<>(finalFile.getLicenses()));
                    // Merge other field
                    if (StringUtils.isBlank(finalFile.getComponentFilePath()))
                        finalFile.setComponentFilePath(file.getComponentFilePath());
                    if (StringUtils.isBlank(finalFile.getComponentFileUrl()))
                        finalFile.setComponentFileUrl(file.getComponentFileUrl());
                    if (StringUtils.isBlank(finalFile.getComponentUrl()))
                        finalFile.setComponentUrl(file.getComponentUrl());
                    isUpdated = true;
                    break;
                }
            }
            // if component is missing in final result - add whole scan result
            if (!isUpdated) {
                resultMergeTo.add(file);
            }
        }
        return resultMergeTo;
    }
}
