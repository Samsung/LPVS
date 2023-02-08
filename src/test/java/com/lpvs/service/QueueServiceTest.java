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
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.util.WebhookUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueueServiceTest {

    private static Logger LOG = LoggerFactory.getLogger(QueueServiceTest.class);

    @Nested
    class TestQueueMethods {

        QueueService queueService;

        WebhookConfig whConfig1;
        WebhookConfig whConfig2;
        WebhookConfig whConfig3;
        WebhookConfig whConfig4;
        WebhookConfig whConfig5;

        @BeforeEach
        void setUp() {
            queueService = new QueueService(null, null, null, 4);

            whConfig1 = new WebhookConfig();
            whConfig1.setId(1L);

            whConfig2 = new WebhookConfig();
            whConfig2.setId(2L);

            whConfig3 = new WebhookConfig();
            whConfig3.setId(3L);

            whConfig4 = new WebhookConfig();
            whConfig4.setId(4L);

            whConfig5 = new WebhookConfig();
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
                LOG.error("InterruptedException at QueueServiceTest.testQueueMethods(): " + e);
                fail();
            }
        }
    }

    @Nested
    class TestProcessWebHook__NoPRDownloaded {
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfig;
        LPVSPullRequest lpvsPullRequest;
        int maxAttempts = 4;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfig.setPullRequestFilesUrl("http://test_url/url");
            webhookConfig.setDate(date);

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfig)).thenReturn(null);

            mockDetectService = mock(DetectService.class);
            mockLicenseService = mock(LicenseService.class);

            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            maxAttempts);
        }

        @Test
        public void testProcessWebHook__NoPRDownloaded() {
            // main test
            queueService.processWebHook(webhookConfig);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfig);
            verify(mockGitHubService, times(1)).commentResults(eq(webhookConfig), anyList(), anyList(), eq(lpvsPullRequest));
            verify(mockGitHubService, times(1)).setErrorCheck(webhookConfig);

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
    static final String snippetMatch_1 = "test_snippet_match_1";
    static final String matchedLines_1 = "test_matched_lines_1";
    static final Set<LPVSLicense> licenses_1 = new HashSet<>(Collections.singletonList(lpvsLicenseTest));
    static final String component_1 = "test_component_1";
    static final LPVSFile lpvsFileTest_1 = new LPVSFile(id_1, fileUrl_1, filePath_1, snippetMatch_1, matchedLines_1, licenses_1, component_1, null, null, null, null, null);

    // LPVSFile-2
    static final Long id_2 = 2L;
    static final String fileUrl_2 = "test_file_url_2";
    static final String filePath_2 = "test_file_path_2";
    static final String snippetMatch_2 = "test_snippet_match_2";
    static final String matchedLines_2 = "test_matched_lines_2";
    static final Set<LPVSLicense> licenses_2 = new HashSet<>(Collections.singletonList(lpvsLicenseTest));
    static final String component_2 = "test_component_2";
    static final LPVSFile lpvsFileTest_2 = new LPVSFile(id_2, fileUrl_2, filePath_2, snippetMatch_2, matchedLines_2, licenses_2, component_2, null, null, null, null, null);

    static final List<LPVSFile> LPVSFilesTest = Arrays.asList(lpvsFileTest_1, lpvsFileTest_2);

    @Nested
    class TestProcessWebHook__DeletionAbsentLicensePresent {
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            webhookConfigMain = new WebhookConfig();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(DetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            4);
        }

        @Test
        public void testProcessWebHook____DeletionAbsentLicensePresent() {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(2)).checkLicense(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
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
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            webhookConfigMain = new WebhookConfig();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(DetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestWithDeletionTruncated)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            4);
        }

        @Test
        public void testProcessWebHook____DeletionPresentLicensePresent() {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(2)).checkLicense(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
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
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            webhookConfigMain = new WebhookConfig();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(DetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseFound() {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(2)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
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
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            webhookConfigMain = new WebhookConfig();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(lpvsLicenseTest);

            mockDetectService = mock(DetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestWithDeletionTruncated)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());


            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionPresentLicenseFound() {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(2)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
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
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            webhookConfigMain = new WebhookConfig();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestNoDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(null);

            mockDetectService = mock(DetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestNoDeletion)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseNull() {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(1)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestNoDeletion);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
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
        QueueService queueService;
        GitHubService mockGitHubService;
        DetectService mockDetectService;
        LicenseService mockLicenseService;
        WebhookConfig webhookConfigMain;
        LPVSPullRequest lpvsPullRequest;
        Date date = new Date();

        @BeforeEach
        void setUp() {
            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("test_url/url");
            lpvsPullRequest.setDate(date);
            lpvsPullRequest.setPullRequestUrl("http://test_url/url/pull/1");
            lpvsPullRequest.setPullRequestFilesUrl("http://test_url/url");

            webhookConfigMain = new WebhookConfig();
            webhookConfigMain.setPullRequestUrl("http://test_url/url/pull/1");
            webhookConfigMain.setPullRequestFilesUrl("http://test_url/url");
            webhookConfigMain.setDate(date);

            mockGitHubService = mock(GitHubService.class);
            when(mockGitHubService.getPullRequestFiles(webhookConfigMain)).thenReturn(filePathTestWithDeletion);
            when(mockGitHubService.getRepositoryLicense(webhookConfigMain)).thenReturn(licenseNameTest);

            mockLicenseService = mock(LicenseService.class);
            when(mockLicenseService.checkLicense(licenseNameTest)).thenReturn(null);
            when(mockLicenseService.findLicenseByName(licenseNameTest)).thenReturn(null);

            mockDetectService = mock(DetectService.class);
            try {
                when(mockDetectService.runScan(webhookConfigMain, filePathTestWithDeletionTruncated)).thenReturn(LPVSFilesTest);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
                fail();
            }

            when(mockLicenseService.findConflicts(webhookConfigMain, LPVSFilesTest)).thenReturn(Collections.emptyList());

            queueService = new QueueService(mockGitHubService,
                                            mockDetectService,
                                            mockLicenseService,
                                            4);
        }

        @Test
        public void testProcessWebHook__DeletionAbsentLicenseNull() {
            // main test
            queueService.processWebHook(webhookConfigMain);

            verify(mockGitHubService, times(1)).getPullRequestFiles(webhookConfigMain);
            verify(mockGitHubService, times(1)).getRepositoryLicense(webhookConfigMain);
            verify(mockLicenseService, times(1)).checkLicense(licenseNameTest);
            verify(mockLicenseService, times(1)).findLicenseByName(licenseNameTest);
            try {
                verify(mockDetectService, times(1)).runScan(webhookConfigMain, filePathTestWithDeletionTruncated);
            } catch (Exception e) {
                LOG.error("TestProcessWebHook__DeletionAbsentLicensePresent: Exception: " + e);
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
