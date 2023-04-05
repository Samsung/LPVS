/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestStatus;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.util.LPVSWebhookUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
public class LPVSQueueServiceTest {

    @Nested
    class TestQueueMethods {

        LPVSQueueService queueService;

        LPVSQueue whConfig1;
        LPVSQueue whConfig2;
        LPVSQueue whConfig3;
        LPVSQueue whConfig4;
        LPVSQueue whConfig5;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);

        @BeforeEach
        void setUp() {
            queueService = new LPVSQueueService(null, null, null, mocked_lpvsPullRequestRepository, mocked_queueRepository, 4);

            whConfig1 = new LPVSQueue();
            whConfig1.setId(1L);

            whConfig2 = new LPVSQueue();
            whConfig2.setId(2L);

            whConfig3 = new LPVSQueue();
            whConfig3.setId(3L);

            whConfig4 = new LPVSQueue();
            whConfig4.setId(4L);

            whConfig5 = new LPVSQueue();
            whConfig5.setId(5L);
        }

        @Test
        public void testQueueMethods() {
            try {
                queueService.addFirst(whConfig1);
                queueService.addFirst(whConfig2);
                queueService.addFirst(whConfig3);
                queueService.addFirst(whConfig4);
                queueService.addFirst(whConfig5);

                assertEquals(whConfig5, queueService.getQueue().take());

                // `whConfig`s 1-4 are left in Queue
                queueService.delete(whConfig4);
                assertEquals(whConfig3, queueService.getQueue().take());

            } catch (InterruptedException e) {
                log.error("InterruptedException at LPVSQueueServiceTest.testQueueMethods(): " + e);
                fail();
            }
        }
    }

    @Nested
    class TestProcessWebHook__NoPRDownloaded {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfig;
        LPVSPullRequest lpvsPullRequest;
        int maxAttempts = 4;
        Date date = new Date();

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

            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            maxAttempts);
        }

        @Test
        public void testProcessWebHook__NoPRDownloaded() throws IOException {
            // main test
            queueService.processWebHook(webhookConfig);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfig);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfig), any(), any(), eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }

    // ==== constants common for next 6 tests ====

    // case DeletionAbsent
    static final String filePathTestNoDeletion = "Projects/Samsung/LPVS/3c688f5caa42b936cd6bea04c1dc3f732364250b";

    // case DeletionPresent
    static final String filePathTestWithDeletion = "Projects/Samsung/LPVS/3c688f5caa42b936cd6bea04c1dc3f732364250b";
    static final String filePathTestWithDeletionTruncated = "Projects/Samsung/LPVS/3c688f5caa42b936cd6bea04c1dc3f732364250b";

    // LPVSLicense
    static final String licenseNameTest = "test_license_name";
    static final String spdxIdTest = "test_spdx_id";
    static final String accessTest = "test_access";
    static final String alternativeNameTest = "test_alternative_name";
    static final String checklistUrlTest = "test_checklist_url";
    static final LPVSLicense lpvsLicenseTest = new LPVSLicense(42L, licenseNameTest, spdxIdTest, accessTest, alternativeNameTest, checklistUrlTest);

    // LPVSFile-1
    static final Long id_1 = 1L;
    static final String fileUrl_1 = "test_file_url_1";
    static final String filePath_1 = "test_file_path_1";
    static final String snippetType_1 = "test_snippet_type_1";
    static final String snippetMatch_1 = "test_snippet_match_1";
    static final String matchedLines_1 = "test_matched_lines_1";
    static final Set<LPVSLicense> licenses_1 = new HashSet<>(Collections.singletonList(lpvsLicenseTest));
    static final String component_1 = "test_component_1";
    static final LPVSFile lpvsFileTest_1 = new LPVSFile(id_1, filePath_1, snippetType_1, snippetMatch_1, matchedLines_1, licenses_1, component_1, null, null, null, null, null, null);

    // LPVSFile-2
    static final Long id_2 = 2L;
    static final String fileUrl_2 = "test_file_url_2";
    static final String filePath_2 = "test_file_path_2";
    static final String snippetType_2 = "test_snippet_type_2";
    static final String snippetMatch_2 = "test_snippet_match_2";
    static final String matchedLines_2 = "test_matched_lines_2";
    static final Set<LPVSLicense> licenses_2 = new HashSet<>(Collections.singletonList(lpvsLicenseTest));
    static final String component_2 = "test_component_2";
    static final LPVSFile lpvsFileTest_2 = new LPVSFile(id_2, filePath_2, snippetType_2, snippetMatch_2, matchedLines_2, licenses_2, component_2, null, null, null, null, null, null);

    static final List<LPVSFile> LPVSFilesTest = Arrays.asList(lpvsFileTest_1, lpvsFileTest_2);

    @Nested
    class TestProcessWebHook__DeletionAbsentLicensePresent {
        LPVSQueueService queueService;
        LPVSGitHubService mockGitHubService;
        LPVSDetectService mockDetectService;
        LPVSLicenseService mockLicenseService;
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

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
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            4);
        }

        @Test
        public void testProcessWebHook____DeletionAbsentLicensePresent() throws IOException {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(2)).checkLicense(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfigMain), eq(LPVSFilesTest), eq(Collections.emptyList()), eq(lpvsPullRequest));

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
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

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
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestWithDeletionTruncated)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            4);
        }

        @Test
        public void testProcessWebHook____DeletionPresentLicensePresent() throws IOException {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(2)).checkLicense(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfigMain), eq(LPVSFilesTest), eq(Collections.emptyList()), eq(lpvsPullRequest));

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
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

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
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            when(mocked_lpvsPullRequestRepository.saveAndFlush(Mockito.any(LPVSPullRequest.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseFound() throws IOException {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(2)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfigMain), eq(LPVSFilesTest), eq(Collections.emptyList()), eq(lpvsPullRequest));

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
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

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
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestWithDeletionTruncated)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());


            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionPresentLicenseFound() throws IOException {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(2)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfigMain), eq(LPVSFilesTest), eq(Collections.emptyList()), eq(lpvsPullRequest));

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
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

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
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(null);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseNull() throws IOException {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(1)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfigMain), eq(LPVSFilesTest), eq(Collections.emptyList()), eq(lpvsPullRequest));

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
        LPVSPullRequestRepository mocked_lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);
        LPVSQueue webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

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
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LPVSLicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(null);

            mockDetectService = mock(LPVSDetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestWithDeletionTruncated)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new LPVSQueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            mocked_lpvsPullRequestRepository,
                                            mocked_queueRepository,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseNull() throws IOException {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(1)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                log.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }
            verify(mockLicenseService, times(1)).findConflicts(webhookConfigMain, LPVSFilesTest);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfigMain), eq(LPVSFilesTest), eq(Collections.emptyList()), eq(lpvsPullRequest));

            verifyNoMoreInteractions(mockGitHubService);
            verifyNoMoreInteractions(mockDetectService);
            verifyNoMoreInteractions(mockLicenseService);
        }
    }
}
