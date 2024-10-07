/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service.webhook;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestStatus;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSLicenseService;
import com.lpvs.service.LPVSQueueService;
import com.lpvs.service.scan.LPVSDetectService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@Slf4j
public class LPVSWebhookServiceImplTest {

    @Nested
    class TestProcessWebHook__NoPRDownloaded {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfig;
        LPVSPullRequest lpvsPullRequest;
        int maxAttempts = 4;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService;

        @BeforeEach
        void setUp() {
            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfig.setPullRequestFilesUrl("http://test_url/url");
            webhookConfig.setRepositoryUrl("http://test_url/url");
            webhookConfig.setUserId("user");
            webhookConfig.setDate(date);
            webhookConfig.setRepositoryUrl("http://test_url/url");

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfig)).thenReturn(null);

            mockDetectService = mock(LPVSDetectService.class);
            mockLicenseService = mock(LPVSLicenseService.class);

            queueService = new LPVSQueueService(mocked_queueRepository);
            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);
            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook__NoPRDownloaded() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfig);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfig);

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);

            // test when max attempts reached
            webhookConfig.setAttempts(maxAttempts);
            webhookService.processWebHook(webhookConfig);

            verify(mockGitHubService, times(2)).getPullRequestFiles(webhookConfig);
            verify(mockGitHubService, times(1))
                    .commentResults(eq(webhookConfig), any(), any(), eq(lpvsPullRequest));
        }
    }

    // ==== constants common for next 6 tests ====

    // case DeletionAbsent
    static String filePathTestNoDeletion =
            System.getProperty("user.dir")
                    + "/LPVS/Projects/Samsung/LPVS/3c688f5caa42b936cd6bea04c1dc3f732364250b";

    // case DeletionPresent
    static String filePathTestWithDeletion =
            System.getProperty("user.dir")
                    + "/LPVS/Projects/Samsung/LPVS/3c688f5caa42b936cd6bea04c1dc3f732364250b";
    static String filePathTestWithDeletionTruncated =
            System.getProperty("user.dir")
                    + "/LPVS/Projects/Samsung/LPVS/3c688f5caa42b936cd6bea04c1dc3f732364250b";

    private Path tempFolderPath;

    // LPVSLicense
    static final String licenseNameTest = "test_license_name";
    static final String spdxIdTest = "test_spdx_id";
    static final String accessTest = "test_access";
    static final String alternativeNameTest = "test_alternative_name";
    static final String checklistUrlTest = "test_checklist_url";
    static final LPVSLicense lpvsLicenseTest =
            new LPVSLicense(
                    42L,
                    licenseNameTest,
                    spdxIdTest,
                    accessTest,
                    alternativeNameTest,
                    checklistUrlTest);

    // LPVSFile-1
    static final Long id_1 = 1L;
    static final String filePath_1 = "test_file_path_1";
    static final String absolute_filePath_1 = "test_file_path_1";
    static final String snippetType_1 = "test_snippet_type_1";
    static final String snippetMatch_1 = "test_snippet_match_1";
    static final String matchedLines_1 = "test_matched_lines_1";
    static final Set<LPVSLicense> licenses_1 =
            new HashSet<>(Collections.singletonList(lpvsLicenseTest));
    static final String component_1 = "test_component_1";
    static final LPVSFile lpvsFileTest_1 =
            new LPVSFile(
                    id_1,
                    filePath_1,
                    absolute_filePath_1,
                    snippetType_1,
                    snippetMatch_1,
                    matchedLines_1,
                    licenses_1,
                    component_1,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

    // LPVSFile-2
    static final Long id_2 = 2L;
    static final String filePath_2 = "test_file_path_2";
    static final String absoluteFilePath_2 = "test_file_path_2";
    static final String snippetType_2 = "test_snippet_type_2";
    static final String snippetMatch_2 = "test_snippet_match_2";
    static final String matchedLines_2 = "test_matched_lines_2";
    static final Set<LPVSLicense> licenses_2 =
            new HashSet<>(Collections.singletonList(lpvsLicenseTest));
    static final String component_2 = "test_component_2";
    static final LPVSFile lpvsFileTest_2 =
            new LPVSFile(
                    id_2,
                    filePath_2,
                    absoluteFilePath_2,
                    snippetType_2,
                    snippetMatch_2,
                    matchedLines_2,
                    licenses_2,
                    component_2,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

    static final List<LPVSFile> LPVSFilesTest = Arrays.asList(lpvsFileTest_1, lpvsFileTest_2);

    @BeforeEach
    void setUp() {
        // for next tests, create temp dir with temp file
        tempFolderPath = Paths.get(filePathTestNoDeletion);
        try {
            if (!Files.exists(tempFolderPath)) {
                Files.createDirectories(tempFolderPath);
            }
            Path emptyFilePath = tempFolderPath.resolve("dummyFile");
            if (!Files.exists(emptyFilePath)) {
                Files.createFile(emptyFilePath);
            }
        } catch (Exception e) {
            log.warn("error creating temp folder/file " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up the temporary folder after each test
        Files.walk(tempFolderPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Nested
    class TestProcessWebHook__DeletionAbsentLicensePresent {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService;

        @BeforeEach
        void setUp() {
            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfigMain = new LPVSQueue();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setRepositoryUrl("http://test_url/url");
            webhookConfigMain.setUserId("user");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain))
                    .thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain))
                    .thenReturn(new String[] {spdxIdTest, licenseNameTest});

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.getLicenseBySpdxIdAndName(
                            spdxIdTest, Optional.of(licenseNameTest)))
                    .thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion))
                        .thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest))
                    .thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mocked_queueRepository);
            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);
            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook____DeletionAbsentLicensePresent() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1))
                    .getLicenseBySpdxIdAndName(spdxIdTest, Optional.of(licenseNameTest));
            try {
                verify(mockDetectService, times(1))
                        .runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1))
                    .commentResults(
                            eq(webhookConfigMain),
                            eq(LPVSFilesTest),
                            eq(Collections.emptyList()),
                            eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }

    @Nested
    class TestProcessWebHook__DeletionPresentLicensePresent {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService;

        @BeforeEach
        void setUp() {
            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfigMain = new LPVSQueue();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setRepositoryUrl("http://test_url/url");
            webhookConfigMain.setUserId("user");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain))
                    .thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain))
                    .thenReturn(new String[] {spdxIdTest, licenseNameTest});

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.getLicenseBySpdxIdAndName(
                            spdxIdTest, Optional.of(licenseNameTest)))
                    .thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(
                                webhookConfigMain, filePathTestWithDeletionTruncated))
                        .thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest))
                    .thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mocked_queueRepository);
            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);
            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook____DeletionPresentLicensePresent() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1))
                    .getLicenseBySpdxIdAndName(spdxIdTest, Optional.of(licenseNameTest));
            try {
                verify(mockDetectService, times(1))
                        .runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1))
                    .commentResults(
                            eq(webhookConfigMain),
                            eq(LPVSFilesTest),
                            eq(Collections.emptyList()),
                            eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }

    @Nested
    class TestProcessWebHook__DeletionAbsentLicenseFound {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService;

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfigMain = new LPVSQueue();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setRepositoryUrl("http://test_url/url");
            webhookConfigMain.setUserId("user");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain))
                    .thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain))
                    .thenReturn(new String[] {spdxIdTest, licenseNameTest});

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.getLicenseBySpdxIdAndName(
                            spdxIdTest, Optional.of(licenseNameTest)))
                    .thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion))
                        .thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest))
                    .thenReturn(Collections.emptyList());

            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            queueService = new LPVSQueueService(mocked_queueRepository);

            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);

            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseFound() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1))
                    .getLicenseBySpdxIdAndName(spdxIdTest, Optional.of(licenseNameTest));
            try {
                verify(mockDetectService, times(1))
                        .runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1))
                    .commentResults(
                            eq(webhookConfigMain),
                            eq(LPVSFilesTest),
                            eq(Collections.emptyList()),
                            eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }

    @Nested
    class TestProcessWebHook__DeletionPresentLicenseFound {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService = mock(LPVSWebhookService.class);

        @BeforeEach
        void setUp() {
            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfigMain = new LPVSQueue();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setRepositoryUrl("http://test_url/url");
            webhookConfigMain.setUserId("user");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain))
                    .thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain))
                    .thenReturn(new String[] {spdxIdTest, licenseNameTest});

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.getLicenseBySpdxIdAndName(
                            spdxIdTest, Optional.of(licenseNameTest)))
                    .thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(
                                webhookConfigMain, filePathTestWithDeletionTruncated))
                        .thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest))
                    .thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mocked_queueRepository);
            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);
            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook__DeletionPresentLicenseFound() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1))
                    .getLicenseBySpdxIdAndName(spdxIdTest, Optional.of(licenseNameTest));
            try {
                verify(mockDetectService, times(1))
                        .runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionPresentLicenseFound: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1))
                    .commentResults(
                            eq(webhookConfigMain),
                            eq(LPVSFilesTest),
                            eq(Collections.emptyList()),
                            eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }

    @Nested
    class TestProcessWebHook__DeletionAbsentLicenseNull {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService;

        @BeforeEach
        void setUp() {
            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfigMain = new LPVSQueue();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setRepositoryUrl("http://test_url/url");
            webhookConfigMain.setUserId("user");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain))
                    .thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(null);

            mockLicenseService = mock(LPVSLicenseService.class);
            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion))
                        .thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest))
                    .thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mocked_queueRepository);
            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);
            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseNull() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            try {
                verify(mockDetectService, times(1))
                        .runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1))
                    .commentResults(
                            eq(webhookConfigMain),
                            eq(LPVSFilesTest),
                            eq(Collections.emptyList()),
                            eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }

    @Nested
    class TestProcessWebHook__DeletionPresentLicenseNull {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();
        LPVSWebhookServiceFactory mocked_webhookServiceFactory =
                mock(LPVSWebhookServiceFactory.class);
        LPVSWebhookService webhookService;

        @BeforeEach
        void setUp() {
            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.SCANNING.toString());
            lpvsPullRequest.setUser("user");

            webhookConfigMain = new LPVSQueue();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setRepositoryUrl("http://test_url/url");
            webhookConfigMain.setUserId("user");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(LPVSGitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain))
                    .thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(null);

            mockLicenseService = mock(LPVSLicenseService.class);
            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(
                                webhookConfigMain, filePathTestWithDeletionTruncated))
                        .thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest))
                    .thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mocked_queueRepository);
            webhookService =
                    new LPVSWebhookServiceImpl(
                            mockDetectService,
                            mockLicenseService,
                            mockGitHubService,
                            queueService,
                            mocked_queueRepository,
                            mocked_lpvsPullRequestRepository,
                            4);
            when(mocked_webhookServiceFactory.createWebhookService(false))
                    .thenReturn(webhookService);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseNull() throws Exception {
            // main test
            webhookService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            try {
                verify(mockDetectService, times(1))
                        .runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1))
                    .commentResults(
                            eq(webhookConfigMain),
                            eq(LPVSFilesTest),
                            eq(Collections.emptyList()),
                            eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }
}
