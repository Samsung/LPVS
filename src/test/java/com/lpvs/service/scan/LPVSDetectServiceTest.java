/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.scan;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.scan.scanner.LPVSScanossDetectService;
import com.lpvs.util.LPVSCommentUtil;

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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
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
        GitHub mockGitHub = mock(GitHub.class);
        GHCommitPointer mockCommitPointer = mock(GHCommitPointer.class);
        GHRepository mockRepository = mock(GHRepository.class);
        GHPullRequest mockPullRequest = mock(GHPullRequest.class);
        GHRepository mockHeadRepository = mock(GHRepository.class);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);

        LPVSQueue webhookConfig;
        final String test_path = "test_path";

        LPVSFile lpvs_file_1, lpvs_file_2;

        @BeforeEach
        void setUp() throws IOException {
            detectService =
                    new LPVSDetectService(
                            "scanoss",
                            false,
                            github_mock,
                            licenseservice_mock,
                            githubservice_mock,
                            scanServiceFactory_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            lpvs_file_1 =
                    new LPVSFile(
                            1L, null, null, null, null, null, null, null, null, null, null, null,
                            null, null);
            lpvs_file_2 =
                    new LPVSFile(
                            2L, null, null, null, null, null, null, null, null, null, null, null,
                            null, null);

            when(scanoss_mock.checkLicenses(webhookConfig))
                    .thenReturn(List.of(lpvs_file_1, lpvs_file_2));
            when(github_mock.connectToGitHubApi()).thenReturn(mockGitHub);
        }

        @Test
        void testRunOneScanWithNullTrigger() throws NoSuchFieldException, IllegalAccessException {
            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss", false, null, null, null, scanServiceFactory_mock));

            setPrivateField(lpvsDetectService, "trigger", null);

            assertDoesNotThrow(() -> lpvsDetectService.runOneScan());

            setPrivateField(lpvsDetectService, "trigger", "");

            assertDoesNotThrow(() -> lpvsDetectService.runOneScan());
        }

        @Test
        void testRunOneScan_Default() throws NoSuchFieldException, IllegalAccessException {

            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss", false, null, null, null, scanServiceFactory_mock));

            setPrivateField(lpvsDetectService, "trigger", "fake-trigger-value");
            setPrivateField(lpvsDetectService, "ctx", mockApplicationContext);

            assertDoesNotThrow(() -> lpvsDetectService.runOneScan());
        }

        @Test
        void testRunOneScan_Branch2()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSLicenseService.Conflict<String, String> conflict_1 =
                    new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);

            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss", false, null, null, null, scanServiceFactory_mock));

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", null);
            setPrivateField(detectService, "ctx", mockApplicationContext);

            detectService.runOneScan();

            setPrivateField(detectService, "htmlReport", "");

            detectService.runOneScan();
        }

        @Test
        void testRunOneScan_Branch3()
                throws NoSuchFieldException, IllegalAccessException, IOException {
            LPVSLicenseService.Conflict<String, String> conflict_1 =
                    new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);

            lpvsDetectService =
                    spy(
                            new LPVSDetectService(
                                    "scanoss", false, null, null, null, scanServiceFactory_mock));

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "build");
            setPrivateField(detectService, "ctx", mockApplicationContext);

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));

            detectService.runOneScan();

            assertDoesNotThrow(() -> detectService.runOneScan());
        }

        @Test
        void testRunOneScan_trigerInternalQueueException()
                throws NoSuchFieldException, IllegalAccessException {

            setPrivateField(detectService, "trigger", "fake-trigger-value");
            setPrivateField(detectService, "ctx", mockApplicationContext);

            assertDoesNotThrow(() -> detectService.runOneScan());
        }

        @Test
        void testRunOneScan_TriggerNotNull() throws Exception {

            LPVSLicenseService.Conflict<String, String> conflict_1 =
                    new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "build/report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));

            detectService.runOneScan();

            assertDoesNotThrow(() -> detectService.runOneScan());
        }

        @Test
        void testRunOneScan_TriggerNotNull_Branch2() throws Exception {

            LPVSLicenseService.Conflict<String, String> conflict_1 =
                    new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "build/report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(null);
            when(mockHeadRepository.getHtmlUrl())
                    .thenReturn(new URL("https://example.com/repo/files"));

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockPullRequest.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));

            detectService.runOneScan();

            assertDoesNotThrow(() -> detectService.runOneScan());
        }

        @Test
        void testRunOneScan_TriggerNotNull_Branch3() throws Exception {
            GHRepository mockHeadRepository2 = mock(GHRepository.class);

            LPVSLicenseService.Conflict<String, String> conflict_1 =
                    new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);

            setPrivateField(detectService, "trigger", "github/owner/repo/branch/123");
            setPrivateField(detectService, "htmlReport", "build/report/test.html");
            setPrivateField(detectService, "ctx", mockApplicationContext);

            // Mock the necessary GitHub objects for LPVSQueue
            when(mockGitHub.getRepository(any())).thenReturn(mockRepository);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockRepository.getPullRequest(anyInt())).thenReturn(mockPullRequest);
            when(mockPullRequest.getHead()).thenReturn(mockCommitPointer);
            when(licenseservice_mock.findConflicts(webhookConfig, null)).thenReturn(expected);
            when(mockCommitPointer.getRepository()).thenReturn(mockHeadRepository2);

            // Set up expected values
            String expectedPullRequestUrl = "https://example.com/pull/1";
            when(mockRepository.getHtmlUrl()).thenReturn(new URL(expectedPullRequestUrl));

            detectService.runOneScan();

            assertDoesNotThrow(() -> detectService.runOneScan());
        }

        @Test
        void testCommentBuilder_ConflictFilePresent() throws Exception {

            LPVSLicenseService.Conflict<String, String> conflict_1 =
                    new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");

            List<LPVSLicenseService.Conflict<String, String>> expected =
                    List.of(conflict_1, conflict_1);

            LPVSQueue webhookConfig = new LPVSQueue();
            List<LPVSFile> scanResults = new ArrayList<>();
            String commentGitHub =
                    LPVSCommentUtil.reportCommentBuilder(webhookConfig, scanResults, expected);
            String commentHTML =
                    LPVSCommentUtil.buildHTMLComment(webhookConfig, scanResults, expected);

            assertNotNull(commentGitHub);
            assertNotNull(commentHTML);
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
                setPrivateField(detectService, "scanService", scanoss_mock);
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

        LPVSQueue webhookConfig;
        final String test_path = "test_path";
        final String exc_msg = "Test exception for TestRunScan__ScanossException. Normal behavior.";

        @BeforeEach
        void setUp() {
            detectService =
                    new LPVSDetectService(
                            "scanoss",
                            false,
                            github_mock,
                            licenseservice_mock,
                            githubservice_mock,
                            scanServiceFactory_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);

            try {
                setPrivateField(detectService, "scanService", scanoss_mock);
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
                            "not_scanoss", false, null, null, null, scanServiceFactory_mock);
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
}
