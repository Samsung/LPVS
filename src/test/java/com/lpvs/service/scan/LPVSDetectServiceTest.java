/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.report.LPVSReportBuilder;
import com.lpvs.entity.LPVSConflict;
import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.scan.scanner.LPVSScanossDetectService;

import com.lpvs.util.LPVSFileUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class LPVSDetectServiceTest {

    private LPVSDetectService lpvsDetectService;

    @Nested
    class TestRunScan__Scanoss {
        LPVSDetectService detectService;
        LPVSGitHubConnectionService github_mock = mock(LPVSGitHubConnectionService.class);
        LPVSScanossDetectService scanoss_mock = mock(LPVSScanossDetectService.class);
        LPVSLicenseService licenseservice_mock = mock(LPVSLicenseService.class);
        LPVSGitHubService githubservice_mock = mock(LPVSGitHubService.class);
        LPVSScanServiceFactory scanServiceFactory_mock = mock(LPVSScanServiceFactory.class);
        LPVSReportBuilder reportBuilder_mock = mock(LPVSReportBuilder.class);
        GitHub mockGitHub = mock(GitHub.class);
        GHCommitPointer mockCommitPointer = mock(GHCommitPointer.class);
        GHRepository mockRepository = mock(GHRepository.class);
        GHPullRequest mockPullRequest = mock(GHPullRequest.class);
        GHRepository mockHeadRepository = mock(GHRepository.class);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

        LPVSQueue webhookConfig;
        final String test_path = "test_path";

        LPVSFile lpvs_file_1, lpvs_file_2;
        LPVSLicense lpvs_license;

        @BeforeEach
        void setUp() throws IOException {
            detectService =
                    new LPVSDetectService(
                            "scanoss",
                            "false",
                            github_mock,
                            licenseservice_mock,
                            githubservice_mock,
                            scanServiceFactory_mock,
                            reportBuilder_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            webhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/1");

            lpvs_license =
                    new LPVSLicense(
                            1L,
                            "MIT License",
                            "MIT",
                            "PERMITTED",
                            "",
                            "https://opensource.org/licenses/MIT");

            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            "test_path" + File.separator + "some_path",
                            null,
                            "snippet",
                            "100%",
                            "1-10",
                            new HashSet<>() {
                                {
                                    add(lpvs_license);
                                }
                            },
                            "src/main/java/com/internal/lpvs/util/LPVSWebhookUtilExtension.java",
                            "main/java/com/lpvs/util/LPVSWebhookUtil.java",
                            "LPVS",
                            "1-10",
                            "https://github.com/Samsung/LPVS",
                            "1.0.1",
                            "Samsung");
            lpvs_file_2 =
                    new LPVSFile(
                            2L,
                            "some_path",
                            null,
                            "snippet",
                            "100%",
                            "34-45",
                            new HashSet<>() {
                                {
                                    add(lpvs_license);
                                }
                            },
                            "src/main/java/com/internal/lpvs/util/LPVSWebhookUtilExtension.java",
                            "main/java/com/lpvs/util/LPVSWebhookUtil.java",
                            "LPVS",
                            "34-45",
                            "https://github.com/Samsung/LPVS",
                            "1.5.0",
                            "Samsung");

            when(scanoss_mock.checkLicenses(any())).thenReturn(List.of(lpvs_file_1, lpvs_file_2));
            when(github_mock.connectToGitHubApi()).thenReturn(mockGitHub);
        }

        @Test
        void testRunOneScanPullRequestWithNullTrigger()
                throws NoSuchFieldException, IllegalAccessException {
            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    null));

            setPrivateField(lpvsDetectService, "trigger", null);

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());

            setPrivateField(lpvsDetectService, "trigger", "");

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());
        }

        @Test
        void testRunOneScanLocalFileWithNullTrigger()
                throws NoSuchFieldException, IllegalAccessException {
            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    null));

            setPrivateField(lpvsDetectService, "localPath", null);

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());

            setPrivateField(lpvsDetectService, "localPath", "");

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());
        }

        @Test
        void testRunOneScanBothPullRequestAndLocalFile()
                throws NoSuchFieldException, IllegalAccessException {
            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    null));

            setPrivateField(lpvsDetectService, "ctx", mockApplicationContext);
            setPrivateField(lpvsDetectService, "trigger", "");
            setPrivateField(lpvsDetectService, "localPath", "");

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());
        }

        @Test
        void testRunOneScanBothPullRequestAndLocalFile2()
                throws NoSuchFieldException, IllegalAccessException {
            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    null));

            setPrivateField(lpvsDetectService, "ctx", mockApplicationContext);
            setPrivateField(lpvsDetectService, "trigger", "some-pull-request");
            setPrivateField(lpvsDetectService, "localPath", "some-local-path");

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());
        }

        @Test
        void testRunOneScan_PullRequest_Default()
                throws NoSuchFieldException, IllegalAccessException {

            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    null));

            setPrivateField(lpvsDetectService, "trigger", "fake-trigger-value");
            setPrivateField(lpvsDetectService, "ctx", mockApplicationContext);
            setPrivateField(lpvsDetectService, "gitHubService", githubservice_mock);

            assertDoesNotThrow(() -> lpvsDetectService.runSingleScan());
        }

        @Test
        void testRunOneScan_PullRequest_Branch2()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    reportBuilder_mock));

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");
            when(reportBuilder_mock.generateCommandLineComment(anyString(), anyList(), anyList()))
                    .thenReturn("Sample report");

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", null);
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            assertDoesNotThrow(() -> detectService.runSingleScan());

            setPrivateField(detectService, "htmlReport", "");

            assertDoesNotThrow(() -> detectService.runSingleScan());
        }

        @Test
        void testRunOneScan_Branch3()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss",
                                    "false",
                                    null,
                                    null,
                                    null,
                                    scanServiceFactory_mock,
                                    null));

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "build");
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);
            when(reportBuilder_mock.generateHtmlReportSingleScan(
                            anyString(), anyList(), anyList(), any(), any()))
                    .thenReturn("<html></html>");

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteFile("build");
        }

        @Test
        void testRunOneScan_LocalFiles_WithConsoleReport()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            File sourceDir = Files.createTempDirectory("source").toFile();
            File sourceFile1 = new File(sourceDir, "file1.txt");
            sourceFile1.createNewFile();

            setPrivateField(detectService, "localPath", sourceFile1.getAbsolutePath());
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");
            when(reportBuilder_mock.generateCommandLineComment(anyString(), anyList(), anyList()))
                    .thenReturn("Sample report");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteDirectory(sourceDir.getAbsolutePath());
        }

        @Test
        void testRunOneScan_LocalFiles_WithHtmlReport()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            File sourceDir = Files.createTempDirectory("source").toFile();
            File sourceFile1 = new File(sourceDir, "file1.txt");
            File sourceFile2 = new File(sourceDir, "file2.txt");
            sourceFile1.createNewFile();
            sourceFile2.createNewFile();

            setPrivateField(detectService, "localPath", sourceDir.getAbsolutePath());
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));
            setPrivateField(detectService, "htmlReport", "report/test.html");

            new File("report").mkdir();

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);
            when(reportBuilder_mock.generateHtmlReportSingleScan(
                            anyString(), anyList(), anyList(), any(), any()))
                    .thenReturn("<html></html>");

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteDirectory(sourceDir.getAbsolutePath());
            deleteDirectory("report");
        }

        @Test
        void testRunOneScan_LocalFiles_NoFile()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            File sourceDir = Files.createTempDirectory("source").toFile();
            File sourceFile1 = new File(sourceDir, "file1.txt");

            setPrivateField(detectService, "localPath", sourceFile1.getAbsolutePath());
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteDirectory(sourceDir.getAbsolutePath());
        }

        @Test
        void testRunOneScan_trigerInternalQueueException()
                throws NoSuchFieldException, IllegalAccessException {

            setPrivateField(detectService, "trigger", "fake-trigger-value");
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "reportBuilder", reportBuilder_mock);
            when(reportBuilder_mock.generateCommandLineComment(anyString(), anyList(), anyList()))
                    .thenReturn("Sample report");

            assertDoesNotThrow(() -> detectService.runSingleScan());
        }

        @Test
        void testRunOneScan_TriggerNotNull() throws Exception {

            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            new File("report").mkdir();

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);
            when(reportBuilder_mock.generateHtmlReportSingleScan(
                            anyString(), anyList(), anyList(), any(), any()))
                    .thenReturn("<html></html>");

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteDirectory("report");
        }

        @Test
        void testRunOneScan_TriggerNotNull_Branch2() throws Exception {

            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            new File("report").mkdir();

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(null);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);
            when(reportBuilder_mock.generateHtmlReportSingleScan(
                            anyString(), anyList(), anyList(), any(), any()))
                    .thenReturn("<html></html>");

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteDirectory("report");
        }

        @Test
        void testRunOneScan_TriggerNotNull_Branch3() throws Exception {
            GHRepository mockHeadRepository2 = mock(GHRepository.class);

            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            new File("report").mkdir();

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository2);
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);
            when(reportBuilder_mock.generateHtmlReportSingleScan(
                            anyString(), anyList(), anyList(), any(), any()))
                    .thenReturn("<html></html>");

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockRepository.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());

            deleteDirectory("report");
        }

        @Test
        void testRunOneScan_TriggerNotNull_NoDirectory() throws Exception {
            GHRepository mockHeadRepository2 = mock(GHRepository.class);

            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);
            setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository2);
            when(githubservice_mock.getInternalQueueByPullRequest(anyString()))
                    .thenReturn(webhookConfig);
            when(reportBuilder_mock.generateHtmlReportSingleScan(
                            anyString(), anyList(), anyList(), any(), any()))
                    .thenReturn("<html></html>");

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockRepository.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));
            when(githubservice_mock.getPullRequestFiles(any()))
                    .thenReturn(
                            System.getProperty("user.home")
                                    + File.separator
                                    + "LPVS"
                                    + File.separator
                                    + "Projects");

            assertDoesNotThrow(() -> detectService.runSingleScan());
        }

        @Test
        void testCommentBuilder_ConflictFilePresent() {
            LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
            LPVSConflict<String, String> conflict_1 = new LPVSConflict<>("MIT", "Apache-2.0");

            List<LPVSConflict<String, String>> expected = List.of(conflict_1, conflict_1);

            List<LPVSFile> scanResults = new ArrayList<>();
            String commentGitHub =
                    reportBuilder.generateCommandLineComment(
                            "/some/path/to/file", scanResults, expected);

            assertNotNull(commentGitHub);
        }

        @Test
        void testCommentBuilder_NoConflictNoLicense() {
            LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
            List<LPVSConflict<String, String>> expected = new ArrayList<>();
            List<LPVSFile> scanResults = new ArrayList<>();
            String commentGitHub =
                    reportBuilder.generateCommandLineComment(
                            "/some/path/to/file", scanResults, expected);

            assertNotNull(commentGitHub);
            assertTrue(commentGitHub.contains("No license problems detected."));
            assertTrue(commentGitHub.contains("No license conflicts detected."));
        }

        @Test
        public void testGetPathByPullRequest() {

            LPVSQueue mockWebhookConfig = mock(LPVSQueue.class);

            when(mockWebhookConfig.getRepositoryUrl())
                    .thenReturn("https://github.com/Samsung/LPVS");

            when(mockWebhookConfig.getPullRequestUrl())
                    .thenReturn("https://github.com/Samsung/LPVS/pull/1");

            String result = LPVSFileUtil.getPathByPullRequest(mockWebhookConfig);

            assertNotNull(result);
        }

        @Test
        public void testRunScan__Scanoss() {
            try {
                setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));
                // main test
                assertEquals(
                        List.of(lpvs_file_1, lpvs_file_2),
                        detectService.runScan(webhookConfig, test_path));
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__Scanoss exception: " + e);
                fail();
            }

            // verify `scanoss_mock`
            try {
                verify(scanoss_mock, times(1)).runScan(webhookConfig, test_path);
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__Scanoss exception: " + e);
                fail();
            }
            verify(scanoss_mock, times(1)).checkLicenses(webhookConfig);
            verifyNoMoreInteractions(scanoss_mock);
        }
    }

    @Nested
    class TestRunScan__ScanossException {
        LPVSDetectService detectService;
        LPVSGitHubConnectionService github_mock = mock(LPVSGitHubConnectionService.class);
        LPVSScanossDetectService scanoss_mock = mock(LPVSScanossDetectService.class);
        LPVSLicenseService licenseservice_mock = mock(LPVSLicenseService.class);
        LPVSGitHubService githubservice_mock = mock(LPVSGitHubService.class);
        LPVSScanServiceFactory scanServiceFactory_mock = mock(LPVSScanServiceFactory.class);
        LPVSReportBuilder reportBuilder_mock = mock(LPVSReportBuilder.class);

        LPVSQueue webhookConfig;
        final String test_path = "test_path";
        final String exc_msg = "Test exception for TestRunScan__ScanossException. Normal behavior.";

        @BeforeEach
        void setUp() {
            detectService =
                    new LPVSDetectService(
                            "scanoss",
                            "false",
                            github_mock,
                            licenseservice_mock,
                            githubservice_mock,
                            scanServiceFactory_mock,
                            reportBuilder_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);

            try {
                setPrivateField(detectService, "scanServiceList", List.of(scanoss_mock));
                doThrow(new Exception(exc_msg))
                        .when(scanoss_mock)
                        .runScan(webhookConfig, test_path);
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__ScanossException exception: " + e);
                fail();
            }
        }

        @Test
        public void testRunScan__ScanossException() {
            try {
                // main test
                detectService.runScan(webhookConfig, test_path);
                fail("Should exit by exception");
            } catch (Exception e) {
                if (!e.getMessage().equals(exc_msg)) {
                    fail("Another exception caught: " + e);
                }
                // test pass
            }

            // verify `scanoss_mock`
            try {
                verify(scanoss_mock, times(1)).runScan(webhookConfig, test_path);
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__ScanossException exception: " + e);
                fail();
            }
            verifyNoMoreInteractions(scanoss_mock);
        }
    }

    @Nested
    class TestRunScan__NotScanoss {
        LPVSDetectService detectService;

        LPVSScanServiceFactory scanServiceFactory_mock = mock(LPVSScanServiceFactory.class);

        @BeforeEach
        void setUp() {
            detectService =
                    new LPVSDetectService(
                            "not_scanoss",
                            "false",
                            null,
                            null,
                            null,
                            scanServiceFactory_mock,
                            null);
        }

        @Test
        public void testRunScan__NotScanoss() {
            // main test
            try {
                assertEquals(new ArrayList<>(), detectService.runScan(null, null));
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__NotScanoss exception: " + e);
                fail();
            }
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
        field.setAccessible(false);
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private void deleteDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file.getPath());
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
