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
import com.lpvs.entity.enums.LPVSPullRequestAction;
import com.lpvs.entity.enums.LPVSVcs;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseConflictRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.util.LPVSCommentUtil;
import com.lpvs.util.LPVSExitHandler;
import com.lpvs.util.LPVSFileUtil;
import com.lpvs.util.LPVSWebhookUtil;
import lombok.extern.slf4j.Slf4j;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
public class LPVSGitHubServiceTest {

    @SystemStub private EnvironmentVariables environmentVars;

    /**
     * Helper class to mock `GHPullRequest`, because we cannot mock it via Mockito.
     * There is exception when we try to mock `getUrl()` method:
     *
     *    org.mockito.exceptions.misusing.WrongTypeOfReturnValue:
     *    URL cannot be returned by getUrl()
     *    getUrl() should return String
     */
    private LPVSExitHandler exitHandler;

    @Mock private GitHub gitHub;

    @Mock private GHRepository ghRepository;

    @Mock private GHPullRequest ghPullRequest;

    @Mock private LPVSGitHubConnectionService gitHubConnectionService;

    @InjectMocks private LPVSGitHubService gitHubService;

    static class GHPullRequestOurMock extends GHPullRequest {
        private final URL mockedGetUrl;
        private final String mockedGetTitle;
        private final PagedIterable<GHPullRequestFileDetail> mockedListFiles;
        private final int mockedGetDeletions;
        private final GHCommitPointer mockedGetHead;

        public GHPullRequestOurMock(
                URL mockedGetUrl,
                String mockedGetTitle,
                PagedIterable<GHPullRequestFileDetail> mockedListFiles,
                int mockedGetDeletions,
                GHCommitPointer mockedGetHead) {
            this.mockedGetUrl = mockedGetUrl;
            this.mockedGetTitle = mockedGetTitle;
            this.mockedListFiles = mockedListFiles;
            this.mockedGetDeletions = mockedGetDeletions;
            this.mockedGetHead = mockedGetHead;
        }

        @Override
        public URL getUrl() {
            return mockedGetUrl;
        }

        @Override
        public String getTitle() {
            return mockedGetTitle;
        }

        @Override
        public PagedIterable<GHPullRequestFileDetail> listFiles() {
            return mockedListFiles;
        }

        @Override
        public int getDeletions() {
            return mockedGetDeletions;
        }

        @Override
        public GHCommitPointer getHead() {
            return mockedGetHead;
        }

        static class CommentCall {
            String arg;
            boolean checked = false;

            CommentCall(String arg) {
                this.arg = arg;
            }
        }

        private final List<CommentCall> comment_calls = new ArrayList<>();

        @Override
        public GHIssueComment comment(String message) {
            comment_calls.add(new CommentCall(message));
            return null;
        }

        public void verifyCommentCall(String expected_arg) {
            for (CommentCall c : comment_calls) {
                if (c.arg.equals(expected_arg) && !c.checked) {
                    c.checked = true;
                    return;
                }
            }
            log.error(expected_arg.replace("\n", "\\n"));
            log.error(comment_calls.get(0).arg.replace("\n", "\\n"));

            // if not found
            fail("Call with arg " + expected_arg + " haven't passed verification");
        }

        public void verifyNoMoreCommentCalls() {
            for (CommentCall c : comment_calls) {
                if (!c.checked) {
                    fail("There is still not checked call with arg " + c.arg);
                }
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files =
                new PagedIterable<GHPullRequestFileDetail>() {
                    @Override
                    public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                        return null;
                    }
                };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 =
                        new GHPullRequestOurMock(
                                new URL(url_pr_2),
                                "GithubService::getRepositoryLicense tests",
                                mocked_list_files,
                                0,
                                null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.setUp() error "
                                + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class);
                    MockedStatic<LPVSFileUtil> mocked_static_file_util =
                            mockStatic(LPVSFileUtil.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);
                mocked_static_file_util
                        .when(() -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig),
                        times(1));
                mocked_static_file_util.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files =
                new PagedIterable<GHPullRequestFileDetail>() {
                    @Override
                    public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                        return null;
                    }
                };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        GHPullRequest mocked_pr_3;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            webhookConfig.setPullRequestFilesUrl(url_pr_2);

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 =
                        new GHPullRequestOurMock(
                                new URL(url_pr_2),
                                "GithubService::getRepositoryLicense tests",
                                mocked_list_files,
                                0,
                                null);
                mocked_pr_3 = new GHPullRequestOurMock(null, null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.setUp() error "
                                + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2, mocked_pr_3));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class);
                    MockedStatic<LPVSFileUtil> mocked_static_file_util =
                            mockStatic(LPVSFileUtil.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);
                mocked_static_file_util
                        .when(() -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() error "
                                    + e);
                    fail();
                }

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig),
                        times(1));
                mocked_static_file_util.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescanEmptyAuthToken {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        LPVSGitHubConnectionService lpvsGitHubConnectionService =
                new LPVSGitHubConnectionService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler);

        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        lpvsGitHubConnectionService);
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files =
                new PagedIterable<GHPullRequestFileDetail>() {
                    @Override
                    public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                        return null;
                    }
                };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        GHPullRequest mocked_pr_3;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            webhookConfig.setPullRequestFilesUrl(url_pr_2);

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 =
                        new GHPullRequestOurMock(
                                new URL(url_pr_2),
                                "GithubService::getRepositoryLicense tests",
                                mocked_list_files,
                                0,
                                null);
                mocked_pr_3 = new GHPullRequestOurMock(null, null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.setUp() error "
                                + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2, mocked_pr_3));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class);
                    MockedStatic<LPVSFileUtil> mocked_static_file_util =
                            mockStatic(LPVSFileUtil.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);
                mocked_static_file_util
                        .when(() -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() error "
                                    + e);
                    fail();
                }

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig),
                        times(1));
                mocked_static_file_util.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);

        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String url_pr_3 = "https://github.com/Samsung/LPVS/pull/20";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);
            webhookConfig.setPullRequestUrl(url_pr_3);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan.setUp() error "
                                + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);

        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String url_pr_3 = "https://github.com/Samsung/LPVS/pull/20";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);
            webhookConfig.setPullRequestUrl(url_pr_3);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan.setUp() error "
                                + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        final String url_pr_3 = "https://github.com/Samsung/LPVS/pull/19";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);
            webhookConfig.setPullRequestUrl(url_pr_3);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }

            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan. Normal behavior."));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        final String url_pr_3 = "https://github.com/Samsung/LPVS/pull/19";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);
            webhookConfig.setPullRequestUrl(url_pr_3);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan. Normal behavior."));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "https://api.github.com";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files =
                new PagedIterable<GHPullRequestFileDetail>() {
                    @Override
                    public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                        return null;
                    }
                };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        GHCommitPointer mocked_commit_pointer = mock(GHCommitPointer.class);
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_url = "https://github.com/Samsung/LPVS";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setAction(LPVSPullRequestAction.RESCAN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setRepositoryUrl(repo_url);

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 =
                        new GHPullRequestOurMock(
                                new URL(url_pr_2),
                                "GithubService::getRepositoryLicense tests",
                                mocked_list_files,
                                0,
                                mocked_commit_pointer);
            } catch (MalformedURLException e) {
                log.error(
                        "TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent.setUp() error "
                                + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }

            when(mocked_commit_pointer.getSha()).thenReturn(commit_sha);
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class);
                    MockedStatic<LPVSFileUtil> mocked_static_file_util =
                            mockStatic(LPVSFileUtil.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);
                mocked_static_file_util
                        .when(() -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent.testGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    log.error(
                            "TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent.testGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);

                verify(mocked_commit_pointer, times(0)).getSha();
                verifyNoMoreInteractions(mocked_commit_pointer);

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> LPVSFileUtil.saveGithubDiffs(mocked_list_files, webhookConfig),
                        times(1));
                mocked_static_file_util.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestSetPendingCheck__ApiUrlAbsentNormalExecution {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        LPVSQueue webhookConfig;
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setHeadCommitSHA(head_commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(
                                head_commit_sha,
                                GHCommitState.PENDING,
                                null,
                                "Scanning opensource licenses",
                                "[License Pre-Validation Service]"))
                        .thenReturn(null);
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetPendingCheck__ApiUrlAbsentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestSetPendingCheck__ApiUrlAbsentNormalExecution.testSetPendingCheck__ApiUrlAbsentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1))
                            .createCommitStatus(
                                    head_commit_sha,
                                    GHCommitState.PENDING,
                                    null,
                                    "Scanning opensource licenses",
                                    "[License Pre-Validation Service]");
                } catch (IOException e) {
                    log.error(
                            "TestSetPendingCheck__ApiUrlAbsentNormalExecution.testSetPendingCheck__ApiUrlAbsentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestSetPendingCheck__ApiUrlPresentNormalExecution {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        LPVSQueue webhookConfig;
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setHeadCommitSHA(head_commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(
                                head_commit_sha,
                                GHCommitState.PENDING,
                                null,
                                "Scanning opensource licenses",
                                "[License Pre-Validation Service]"))
                        .thenReturn(null);
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetPendingCheck__ApiUrlPresentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestSetPendingCheck__ApiUrlPresentNormalExecution.testSetPendingCheck__ApiUrlPresentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1))
                            .createCommitStatus(
                                    head_commit_sha,
                                    GHCommitState.PENDING,
                                    null,
                                    "Scanning opensource licenses",
                                    "[License Pre-Validation Service]");
                } catch (IOException e) {
                    log.error(
                            "TestSetPendingCheck__ApiUrlPresentNormalExecution.testSetPendingCheck__ApiUrlPresentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestSetPendingCheck__ApiUrlAbsentCantAuthorize {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
        }

        @Test
        public void testSetPendingCheck__ApiUrlAbsentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestSetPendingCheck__ApiUrlAbsentCantAuthorize. Normal behavior."));

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestSetPendingCheck__ApiUrlPresentCantAuthorize {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
        }

        @Test
        public void testSetPendingCheck__ApiUrlPresentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestSetPendingCheck__ApiUrlPresentCantAuthorize. Normal behavior."));

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestSetErrorCheck__ApiUrlAbsentNormalExecution {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        LPVSQueue webhookConfig;
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setHeadCommitSHA(head_commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(
                                head_commit_sha,
                                GHCommitState.ERROR,
                                null,
                                "Scanning process failed",
                                "[License Pre-Validation Service]"))
                        .thenReturn(null);
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetErrorCheck__ApiUrlAbsentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestSetErrorCheck__ApiUrlAbsentNormalExecution.testSetErrorCheck__ApiUrlAbsentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1))
                            .createCommitStatus(
                                    head_commit_sha,
                                    GHCommitState.ERROR,
                                    null,
                                    "Scanning process failed",
                                    "[License Pre-Validation Service]");
                } catch (IOException e) {
                    log.error(
                            "TestSetErrorCheck__ApiUrlAbsentNormalExecution.testSetErrorCheck__ApiUrlAbsentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestSetErrorCheck__ApiUrlPresentNormalExecution {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        LPVSQueue webhookConfig;
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setHeadCommitSHA(head_commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(
                                head_commit_sha,
                                GHCommitState.ERROR,
                                null,
                                "Scanning process failed",
                                "[License Pre-Validation Service]"))
                        .thenReturn(null);
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetErrorCheck__ApiUrlPresentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error(
                            "TestSetErrorCheck__ApiUrlPresentNormalExecution.testSetErrorCheck__ApiUrlPresentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1))
                            .createCommitStatus(
                                    head_commit_sha,
                                    GHCommitState.ERROR,
                                    null,
                                    "Scanning process failed",
                                    "[License Pre-Validation Service]");
                } catch (IOException e) {
                    log.error(
                            "TestSetErrorCheck__ApiUrlPresentNormalExecution.testSetErrorCheck__ApiUrlPresentNormalExecution() error "
                                    + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);
            }
        }
    }

    @Nested
    class TestSetErrorCheck__ApiUrlAbsentCantAuthorize {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
        }

        @Test
        public void testSetErrorCheck__ApiUrlAbsentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestSetErrorCheck__ApiUrlAbsentCantAuthorize. Normal behavior."));

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestSetErrorCheck__ApiUrlPresentCantAuthorize {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
        }

        @Test
        public void testSetErrorCheck__ApiUrlPresentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestSetErrorCheck__ApiUrlPresentCantAuthorize. Normal behavior."));

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestCommentResults__PrAbsent {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        LPVSQueue webhookConfig;
        LPVSPullRequest lpvsPullRequest;
        final String url_pr_3 = "https://github.com/Samsung/LPVS/pull/20";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("TestCommentResults__PrAbsent.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_3);
            webhookConfig.setPullRequestUrl(url_pr_3);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error("TestCommentResults__PrAbsent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testCommentResults__PrAbsent() throws IOException {
            // main test
            gh_service.commentResults(webhookConfig, null, null, lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error("TestCommentResults__PrAbsent.testCommentResults__PrAbsent() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
            } catch (IOException e) {
                log.error("TestCommentResults__PrAbsent.testCommentResults__PrAbsent() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);
        }
    }

    @Nested
    class TestCommentResults__CantAuthorize {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        LPVSQueue webhookConfig;
        LPVSPullRequest lpvsPullRequest;

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("TestCommentResults__CantAuthorize.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            lpvsPullRequest.setRepositoryName("Samsung/LPVS");
            lpvsPullRequest.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/18");
            lpvsPullRequest.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/18");

            webhookConfig = new LPVSQueue();
            final String url_pr = "https://github.com/Samsung/LPVS/pull/18";
            webhookConfig.setPullRequestUrl(url_pr);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            webhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/18");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenThrow(
                                new IOException(
                                        "Test exception for TestCommentResults__CantAuthorize. Normal behavior."));
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
        }

        @Test
        public void testCommentResults__CantAuthorize() throws IOException {
            // main test
            gh_service.commentResults(webhookConfig, null, null, lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(2))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__CantAuthorize.testCommentResults__CantAuthorize() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);
        }
    }

    @Nested
    class TestCommentResults__ScanResultsEmpty {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        LPVSQueue webhookConfig;
        LPVSPullRequest lpvsPullRequest;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("TestCommentResults__ScanResultsEmpty.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error("TestCommentResults__ScanResultsEmpty.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testCommentResults__ScanResultsEmpty() throws IOException {
            // main test
            gh_service.commentResults(webhookConfig, List.of(), null, lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ScanResultsEmpty.testCommentResults__ScanResultsEmpty() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.SUCCESS,
                                null,
                                "No license issue detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ScanResultsEmpty.testCommentResults__ScanResultsEmpty() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);
        }
    }

    @Nested
    class TestCommentResults__ProhibitedPresentConflictsPresent {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));

        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_url = "https://github.com/Samsung/LPVS";

        LPVSPullRequest lpvsPullRequest;
        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 =
                "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String snippet_type_1 = "snippet";
        final String snippet_match_1 =
                "/**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";
        final String component_file_path_1 =
                "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String component_file_url_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String spdx_id_2 = "GPL-2.0-only";
        final String access_1 = "PROHIBITED";
        final String alternativeName_1 = "";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        // `conflict_1`
        LPVSLicenseService.Conflict<String, String> conflict_1;
        final String conflict_1_l1 = "MIT";
        final String conflict_1_l2 = "Apache-1.0";

        final String expected_comment =
                "**\\[License Pre-Validation Service\\]** Potential license problem(s) detected \n\n"
                        + "**Detected licenses:**\n\n\n"
                        + "**File:** src/main/java/com/lpvs/service/LPVSGitHubService.java\n"
                        + "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> (prohibited)\n"
                        + "**Component:** LPVS::Services (src/main/java/com/lpvs/service/LPVSGitHubService.java)\n"
                        + "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/LPVSGitHubService.java#L1L6\">1-6</a>\n"
                        + "**Snippet Match:** /**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n\n\n\n\n"
                        + "**Detected license conflicts:**\n\n\n"
                        + "<ul><li>MIT and Apache-1.0</li></ul>()</p>";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setAttempts(0);
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setUserId("user");
            webhookConfig.setRepositoryUrl(repo_url);
            webhookConfig.setHubLink("");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 =
                    new LPVSLicense(
                            1L,
                            license_name_1,
                            spdx_id_1,
                            access_1,
                            alternativeName_1,
                            checklist_url_1);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            snippet_type_1,
                            snippet_match_1,
                            matched_lines_1,
                            Set.of(lpvs_license_1),
                            component_file_path_1,
                            component_file_url_1,
                            component_1,
                            null,
                            null,
                            null,
                            null);
            conflict_1 = new LPVSLicenseService.Conflict<>(conflict_1_l1, conflict_1_l2);

            when(mocked_lpvsLicenseRepository.searchBySpdxId(anyString()))
                    .thenReturn(lpvs_license_1);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(anyString()))
                    .thenReturn(null);
        }

        @Test
        public void testCommentResults__ProhibitedPresentConflictsPresent() throws IOException {
            // main test
            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__ProhibitedPresentConflictsPresentLicensePresent()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_1);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_1))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_1,
                                    access_1,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__ProhibitedPresentConflictsPresentLicensePresentAlt()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_2);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_2)).thenReturn(null);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(spdx_id_2))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_2,
                                    access_1,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }
    }

    @Nested
    class TestCommentResults__EmptyPresentConflictsPresent {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        LPVSGitHubConnectionService lpvsGitHubConnectionService =
                mock(LPVSGitHubConnectionService.class);

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        lpvsGitHubConnectionService);

        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_url = "https://github.com/Samsung/LPVS";

        LPVSPullRequest lpvsPullRequest;
        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 =
                "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String snippet_type_1 = "snippet";
        final String snippet_match_1 =
                "/**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";
        final String component_file_path_1 =
                "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String component_file_url_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String spdx_id_2 = "GPL-2.0-only";
        final String access_1 = "";
        final String alternativeName_1 = "";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        // `conflict_1`
        LPVSLicenseService.Conflict<String, String> conflict_1;
        final String conflict_1_l1 = "MIT";
        final String conflict_1_l2 = "Apache-1.0";

        final String expected_comment =
                "**\\[License Pre-Validation Service\\]** Potential license problem(s) detected \n\n"
                        + "**Detected licenses:**\n\n\n"
                        + "**File:** src/main/java/com/lpvs/service/LPVSGitHubService.java\n"
                        + "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> ()\n"
                        + "**Component:** LPVS::Services (src/main/java/com/lpvs/service/LPVSGitHubService.java)\n"
                        + "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/LPVSGitHubService.java#L1L6\">1-6</a>\n"
                        + "**Snippet Match:** /**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n\n\n\n\n"
                        + "**Detected license conflicts:**\n\n\n"
                        + "<ul><li>MIT and Apache-1.0</li></ul>()</p>";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("TestCommentResults__EmptyPresentConflictsPresent.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setAttempts(0);
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setUserId("user");
            webhookConfig.setRepositoryUrl(repo_url);
            webhookConfig.setHubLink("");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error("TestCommentResults__EmptyPresentConflictsPresent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 =
                    new LPVSLicense(
                            1L,
                            license_name_1,
                            spdx_id_1,
                            access_1,
                            alternativeName_1,
                            checklist_url_1);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            snippet_type_1,
                            snippet_match_1,
                            matched_lines_1,
                            Set.of(lpvs_license_1),
                            component_file_path_1,
                            component_file_url_1,
                            component_1,
                            null,
                            null,
                            null,
                            null);
            conflict_1 = new LPVSLicenseService.Conflict<>(conflict_1_l1, conflict_1_l2);

            when(mocked_lpvsLicenseRepository.searchBySpdxId(anyString()))
                    .thenReturn(lpvs_license_1);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(anyString()))
                    .thenReturn(null);
        }

        @Test
        public void testCommentResults__EmptyPresentConflictsPresent() throws IOException {
            // main test
            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__EmptyPresentConflictsPresent.testCommentResults__EmptyPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__EmptyPresentConflictsPresent.testCommentResults__EmptyPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__EmptyPresentConflictsPresentLicensePresent()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_1);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_1))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_1,
                                    access_1,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__EmptyPresentConflictsPresent.testCommentResults__EmptyPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__EmptyPresentConflictsPresent.testCommentResults__EmptyPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__EmptyPresentConflictsPresentLicensePresentAlt()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_2);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_2)).thenReturn(null);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(spdx_id_2))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_2,
                                    access_1,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__EmptyPresentConflictsPresent.testCommentResults__EmptyPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__EmptyPresentConflictsPresent.testCommentResults__EmptyPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }
    }

    @Nested
    class TestCommentResults__UnreviewedPresentConflictsPresent {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        LPVSGitHubConnectionService lpvsGitHubConnectionService =
                mock(LPVSGitHubConnectionService.class);

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        lpvsGitHubConnectionService);

        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_url = "https://github.com/Samsung/LPVS";

        LPVSPullRequest lpvsPullRequest;
        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 =
                "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String snippet_type_1 = "snippet";
        final String snippet_match_1 =
                "/**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";
        final String component_file_path_1 =
                "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String component_file_url_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String spdx_id_2 = "GPL-2.0-only";
        final String alternativeName_1 = "";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        final String access_2 = "UNREVIEWED";

        // `conflict_1`
        LPVSLicenseService.Conflict<String, String> conflict_1;
        final String conflict_1_l1 = "MIT";
        final String conflict_1_l2 = "Apache-1.0";

        final String expected_comment =
                "**\\[License Pre-Validation Service\\]** Potential license problem(s) detected \n\n"
                        + "**Detected licenses:**\n\n\n"
                        + "**File:** src/main/java/com/lpvs/service/LPVSGitHubService.java\n"
                        + "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> (unreviewed)\n"
                        + "**Component:** LPVS::Services (src/main/java/com/lpvs/service/LPVSGitHubService.java)\n"
                        + "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/LPVSGitHubService.java#L1L6\">1-6</a>\n"
                        + "**Snippet Match:** /**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n\n\n\n\n"
                        + "**Detected license conflicts:**\n\n\n"
                        + "<ul><li>MIT and Apache-1.0</li></ul>()</p>";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setAttempts(0);
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setUserId("user");
            webhookConfig.setRepositoryUrl(repo_url);
            webhookConfig.setHubLink("");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 =
                    new LPVSLicense(
                            1L,
                            license_name_1,
                            spdx_id_1,
                            access_2,
                            alternativeName_1,
                            checklist_url_1);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            snippet_type_1,
                            snippet_match_1,
                            matched_lines_1,
                            Set.of(lpvs_license_1),
                            component_file_path_1,
                            component_file_url_1,
                            component_1,
                            null,
                            null,
                            null,
                            null);
            conflict_1 = new LPVSLicenseService.Conflict<>(conflict_1_l1, conflict_1_l2);

            when(mocked_lpvsLicenseRepository.searchBySpdxId(anyString()))
                    .thenReturn(lpvs_license_1);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(anyString()))
                    .thenReturn(null);
        }

        @Test
        public void testCommentResults__RestrictedPresentConflictsPresent() throws IOException {
            // main test
            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.testCommentResults__UnreviewedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.testCommentResults__UnreviewedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__RestrictedPresentConflictsPresentLicensePresent()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_1);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_1))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_1,
                                    access_2,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.testCommentResults__UnreviewedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.testCommentResults__UnreviewedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__RestrictedPresentConflictsPresentLicensePresentAlt()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_2);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_2)).thenReturn(null);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(spdx_id_2))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_2,
                                    access_2,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.testCommentResults__UnreviewedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__UnreviewedPresentConflictsPresent.testCommentResults__UnreviewedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }
    }

    @Nested
    class TestCommentResults__RestrictedPresentConflictsPresent {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        LPVSGitHubConnectionService lpvsGitHubConnectionService =
                mock(LPVSGitHubConnectionService.class);

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        lpvsGitHubConnectionService);

        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";
        final String repo_url = "https://github.com/Samsung/LPVS";

        LPVSPullRequest lpvsPullRequest;
        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 =
                "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String snippet_type_1 = "snippet";
        final String snippet_match_1 =
                "/**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";
        final String component_file_path_1 =
                "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String component_file_url_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String spdx_id_2 = "GPL-2.0-only";
        final String alternativeName_1 = "";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        final String access_2 = "RESTRICTED";

        // `conflict_1`
        LPVSLicenseService.Conflict<String, String> conflict_1;
        final String conflict_1_l1 = "MIT";
        final String conflict_1_l2 = "Apache-1.0";

        final String expected_comment =
                "**\\[License Pre-Validation Service\\]** Potential license problem(s) detected \n\n"
                        + "**Detected licenses:**\n\n\n"
                        + "**File:** src/main/java/com/lpvs/service/LPVSGitHubService.java\n"
                        + "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> (restricted)\n"
                        + "**Component:** LPVS::Services (src/main/java/com/lpvs/service/LPVSGitHubService.java)\n"
                        + "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/LPVSGitHubService.java#L1L6\">1-6</a>\n"
                        + "**Snippet Match:** /**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n\n\n\n\n"
                        + "**Detected license conflicts:**\n\n\n"
                        + "<ul><li>MIT and Apache-1.0</li></ul>()</p>";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setAttempts(0);
            webhookConfig.setAction(LPVSPullRequestAction.OPEN);
            webhookConfig.setUserId("user");
            webhookConfig.setRepositoryUrl(repo_url);
            webhookConfig.setHubLink("");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 =
                    new LPVSLicense(
                            1L,
                            license_name_1,
                            spdx_id_1,
                            access_2,
                            alternativeName_1,
                            checklist_url_1);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            snippet_type_1,
                            snippet_match_1,
                            matched_lines_1,
                            Set.of(lpvs_license_1),
                            component_file_path_1,
                            component_file_url_1,
                            component_1,
                            null,
                            null,
                            null,
                            null);
            conflict_1 = new LPVSLicenseService.Conflict<>(conflict_1_l1, conflict_1_l2);

            when(mocked_lpvsLicenseRepository.searchBySpdxId(anyString()))
                    .thenReturn(lpvs_license_1);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(anyString()))
                    .thenReturn(null);
        }

        @Test
        public void testCommentResults__RestrictedPresentConflictsPresent() throws IOException {
            // main test
            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.testCommentResults__RestrictedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.testCommentResults__RestrictedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__RestrictedPresentConflictsPresentLicensePresent()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_1);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_1))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_1,
                                    access_2,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.testCommentResults__RestrictedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.testCommentResults__RestrictedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }

        @Test
        public void testCommentResults__RestrictedPresentConflictsPresentLicensePresentAlt()
                throws IOException {
            // main test
            webhookConfig.setRepositoryLicense(spdx_id_2);
            when(mocked_lpvsLicenseRepository.searchBySpdxId(spdx_id_2)).thenReturn(null);
            when(mocked_lpvsLicenseRepository.searchByAlternativeLicenseNames(spdx_id_2))
                    .thenReturn(
                            new LPVSLicense(
                                    1L,
                                    license_name_1,
                                    spdx_id_2,
                                    access_2,
                                    alternativeName_1,
                                    checklist_url_1));

            when(mocked_lpvsLicenseRepository.saveAndFlush(Mockito.any(LPVSLicense.class)))
                    .thenAnswer(i -> i.getArguments()[0]);

            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(conflict_1), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.testCommentResults__RestrictedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.FAILURE,
                                null,
                                "Potential license problem(s) detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__RestrictedPresentConflictsPresent.testCommentResults__RestrictedPresentConflictsPresent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }
    }

    @Nested
    class TestCommentResults__ProhibitedAbsentConflictsAbsent {
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        final String url_pr_2 = "https://github.com/Samsung/LPVS/pull/19";

        LPVSPullRequest lpvsPullRequest;
        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 =
                "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String snippet_type_1 = "snippet";
        final String snippet_match_1 =
                "/**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";
        final String component_file_path_1 =
                "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String component_file_url_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String access_1 = "PERMITTED";
        final String alternativeName_1 = "";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        final String expected_comment =
                "**\\[License Pre-Validation Service\\]**  No license issue detected \n\n"
                        + "**Detected licenses:**\n\n\n"
                        + "**File:** src/main/java/com/lpvs/service/LPVSGitHubService.java\n"
                        + "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> (permitted)\n"
                        + "**Component:** LPVS::Services (src/main/java/com/lpvs/service/LPVSGitHubService.java)\n"
                        + "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/LPVSGitHubService.java#L1L6\">1-6</a>\n"
                        + "**Snippet Match:** /**\n"
                        + " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n"
                        + " *\n"
                        + " * Use of this source code is governed by a MIT license that can be\n"
                        + " * found in the LICENSE file.\n"
                        + " */\n\n\n\n\n"
                        + "</p>";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = LPVSGitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error("TestCommentResults__ProhibitedAbsentConflictsAbsent.setUp() error " + e);
                fail();
            }

            lpvsPullRequest = new LPVSPullRequest();

            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(url_pr_2);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            webhookConfig.setUserId("user");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                log.error("TestCommentResults__ProhibitedAbsentConflictsAbsent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN))
                        .thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                log.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 =
                    new LPVSLicense(
                            1L,
                            license_name_1,
                            spdx_id_1,
                            access_1,
                            alternativeName_1,
                            checklist_url_1);
            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            snippet_type_1,
                            snippet_match_1,
                            matched_lines_1,
                            Set.of(lpvs_license_1),
                            component_file_path_1,
                            component_file_url_1,
                            component_1,
                            null,
                            null,
                            null,
                            null);
        }

        @Test
        public void testCommentResults__ProhibitedAbsentConflictsAbsent() throws IOException {
            // main test
            gh_service.commentResults(
                    webhookConfig, List.of(lpvs_file_1), List.of(), lpvsPullRequest);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1))
                        .getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedAbsentConflictsAbsent.testCommentResults__ProhibitedAbsentConflictsAbsent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1))
                        .createCommitStatus(
                                commit_sha,
                                GHCommitState.SUCCESS,
                                null,
                                "No license issue detected",
                                "[License Pre-Validation Service]");
            } catch (IOException e) {
                log.error(
                        "TestCommentResults__ProhibitedAbsentConflictsAbsent.testCommentResults__ProhibitedAbsentConflictsAbsent() error "
                                + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);

            // `mocked_pr_2` verify
            ((GHPullRequestOurMock) mocked_pr_2).verifyCommentCall(expected_comment);
            ((GHPullRequestOurMock) mocked_pr_2).verifyNoMoreCommentCalls();
        }
    }

    @Nested
    class TestGetRepositoryLicense__ApiUrlAbsentLisencePresent {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHLicense mocked_license = mock(GHLicense.class);
        final String test_license_key = "test_license_key";
        final String url_pr = "https://github.com/Samsung/LPVS/pull/19";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(mocked_license);
            } catch (IOException e) {
                log.error("mocked_repo.getLicense error " + e);
            }
            when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlAbsentLisencePresent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertEquals(test_license_key, gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    log.error("mocked_repo.getLicense error " + e);
                }
                verifyNoMoreInteractions(mocked_repo);

                // `mocked_license` verify
                verify(mocked_license, times(1)).getKey();
                verifyNoMoreInteractions(mocked_license);
            }
        }
    }

    @Nested
    class TestGetRepositoryLicense__ApiUrlAbsentLisenceAbsent {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        // GHLicense mocked_license = mock(GHLicense.class);
        // final String test_license_key = "test_license_key";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(null);
            } catch (IOException e) {
                log.error("mocked_repo.getLicense error " + e);
            }
            // when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlAbsentLisenceAbsent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    log.error("mocked_repo.getLicense error " + e);
                }
                verifyNoMoreInteractions(mocked_repo);

                // // `mocked_license` verify
                // verify (mocked_license, times(1)).getKey();
                // verifyNoMoreI// nteractions(mocked_license);
            }
        }
    }

    @Nested
    class TestGetRepositoryLicense__ApiUrlPresentLisencePresent {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHLicense mocked_license = mock(GHLicense.class);
        final String test_license_key = "test_license_key";
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(mocked_license);
            } catch (IOException e) {
                log.error("mocked_repo.getLicense error " + e);
            }
            when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlPresentLisencePresent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertEquals(test_license_key, gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    log.error("mocked_repo.getLicense error " + e);
                }
                verifyNoMoreInteractions(mocked_repo);

                // `mocked_license` verify
                verify(mocked_license, times(1)).getKey();
                verifyNoMoreInteractions(mocked_license);
            }
        }
    }

    @Nested
    class TestGetRepositoryLicense__ApiUrlPresentLisenceAbsent {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        LPVSQueue webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        // GHLicense mocked_license = mock(GHLicense.class);
        // final String test_license_key = "test_license_key";
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            try {
                when(mocked_instance_gh.getRepository(
                                LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                        + "/"
                                        + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                        .thenReturn(mocked_repo);
            } catch (IOException e) {
                log.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(null);
            } catch (IOException e) {
                log.error("mocked_repo.getLicense error " + e);
            }
            // when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlPresentLisenceAbsent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenReturn(mocked_instance_gh);

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1))
                            .getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                } catch (IOException e) {
                    log.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    log.error("mocked_repo.getLicense error " + e);
                }
                verifyNoMoreInteractions(mocked_repo);

                // // `mocked_license` verify
                // verify(mocked_license, times(1)).getKey();
                // verifyNoMoreInteractions(mocked_license);
            }
        }
    }

    @Nested
    class TestGetRepositoryLicense__ApiUrlAbsentCantAuthorize {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlAbsentCantAuthorize() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN))
                        .thenThrow(new IOException("test cant authorize"));

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestGetRepositoryLicense__ApiUrlPresentCantAuthorize {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        new LPVSGitHubConnectionService(
                                GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler));
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        LPVSQueue webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlPresentCantAuthorize() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh
                        .when(
                                () ->
                                        GitHub.connectToEnterpriseWithOAuth(
                                                GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN))
                        .thenThrow(new IOException("test cant authorize"));

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(
                        () ->
                                GitHub.connectToEnterpriseWithOAuth(
                                        GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN),
                        times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestGetMatchedLinesAsLink_NotAll {

        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String pr_url = "https://github.com/Samsung/LPVS/pull/18";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_path_1 = "src/main/java/com/lpvs/service/LPVSGitHubService.java";
        final String matched_lines_1 = "1-6";

        final String expected_result =
                "<a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/LPVSGitHubService.java#L1L6\">1-6</a>";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(pr_url);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            "snippet",
                            null,
                            matched_lines_1,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);
        }

        @Test
        public void testGetMatchedLinesAsLink_NotAll() {
            // main test
            assertEquals(
                    expected_result,
                    LPVSCommentUtil.getMatchedLinesAsLink(
                            webhookConfig, lpvs_file_1, LPVSVcs.GITHUB));
        }
    }

    @Nested
    class TestGetMatchedLinesAsLink_All {
        // `webhookConfig`
        LPVSQueue webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String pr_url = "https://github.com/Samsung/LPVS/pull/18";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_path_1 = "LICENSE";
        final String matched_lines_1 = "all";

        final String expected_result =
                "<a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/LICENSE\">all</a>";

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setPullRequestUrl(pr_url);
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

            lpvs_file_1 =
                    new LPVSFile(
                            1L,
                            file_path_1,
                            "snippet",
                            null,
                            matched_lines_1,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);
        }

        @Test
        public void testGetMatchedLinesAsLink_All() {
            // main test
            assertEquals(
                    expected_result,
                    LPVSCommentUtil.getMatchedLinesAsLink(
                            webhookConfig, lpvs_file_1, LPVSVcs.GITHUB));
        }
    }

    @Nested
    class TestCommentResults {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "my-token";
        final String GH_API_URL = "test_api_url";
        LPVSPullRequestRepository mocked_pullRequestRepository =
                mock(LPVSPullRequestRepository.class);
        LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                mock(LPVSDetectedLicenseRepository.class);
        LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
        LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                mock(LPVSLicenseConflictRepository.class);
        LPVSGitHubConnectionService lpvsGitHubConnectionService =
                new LPVSGitHubConnectionService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler);
        final LPVSGitHubService gh_service =
                new LPVSGitHubService(
                        mocked_pullRequestRepository,
                        mocked_lpvsDetectedLicenseRepository,
                        mocked_lpvsLicenseRepository,
                        mocked_lpvsLicenseConflictRepository,
                        lpvsGitHubConnectionService);
        final String url_pr_1 = "https://github.com/Samsung/LPVS/pull/18";
        LPVSQueue webhookConfig;
        LPVSPullRequest lpvsPullRequest;

        @BeforeEach
        void setUp() {
            webhookConfig = new LPVSQueue();
            webhookConfig.setPullRequestUrl(url_pr_1);
            webhookConfig.setPullRequestAPIUrl("http://url.com");
            webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            lpvsPullRequest = new LPVSPullRequest();
        }

        @Test
        public void testCommentResults() throws IOException {
            GitHub gitHub = Mockito.mock(GitHub.class);
            GHRepository repository = Mockito.mock(GHRepository.class);
            ReflectionTestUtils.setField(gh_service, "gitHub", gitHub);
            System.out.println(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig));
            System.out.println(LPVSWebhookUtil.getRepositoryName(webhookConfig));
            Mockito.when(
                            gitHub.getRepository(
                                    LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)
                                            + "/"
                                            + LPVSWebhookUtil.getRepositoryName(webhookConfig)))
                    .thenReturn(repository);
            LPVSFile file = new LPVSFile();
            LPVSLicense license =
                    new LPVSLicense() {
                        {
                            setChecklistUrl("");
                            setAccess("unrviewed");
                        }
                    };
            file.setLicenses(
                    new HashSet<LPVSLicense>() {
                        {
                            add(license);
                        }
                    });
            file.setFilePath("");
            file.setComponentFilePath("");
            file.setComponentName("");
            file.setComponentLines("");
            file.setComponentUrl("");
            file.setComponentVersion("");
            file.setComponentVendor("");
            file.setSnippetMatch("");
            file.setMatchedLines("");
            file.setSnippetType("");
            List<LPVSFile> fileList =
                    new ArrayList<LPVSFile>() {
                        {
                            add(file);
                        }
                    };
            List<LPVSLicenseService.Conflict<String, String>> conflictList = new ArrayList<>();
            conflictList.add(new LPVSLicenseService.Conflict<>("1", "2"));
            GHPullRequest pullRequest = new GHPullRequest();
            ReflectionTestUtils.setField(pullRequest, "url", "http://url.com");
            List<GHPullRequest> pullRequestList =
                    new ArrayList<GHPullRequest>() {
                        {
                            add(pullRequest);
                        }
                    };
            Mockito.when(repository.getPullRequests(GHIssueState.OPEN)).thenReturn(pullRequestList);
            gh_service.commentResults(webhookConfig, fileList, conflictList, lpvsPullRequest);
            license.setAccess("");
            gh_service.commentResults(webhookConfig, fileList, conflictList, lpvsPullRequest);
            Mockito.verify(gitHub, times(4)).getRepository(Mockito.anyString());
        }

        @Test
        public void testSetGithubTokenFromEnv_WhenEnvVariableIsSet()
                throws IllegalAccessException, NoSuchFieldException {

            environmentVars.set("LPVS_GITHUB_TOKEN", "GitHubTokenValue");
            String githubTokenValue = "GitHubTokenValue";
            lpvsGitHubConnectionService.setGithubTokenFromEnv();
            Field field =
                    lpvsGitHubConnectionService.getClass().getDeclaredField("GITHUB_AUTH_TOKEN");
            field.setAccessible(true);
            String githubRealTokenValue = (String) field.get(lpvsGitHubConnectionService);
            assertEquals(githubTokenValue, githubRealTokenValue);
        }

        @Test
        public void testSetGithubTokenFromEnv_WhenEnvVariableIsNotSet()
                throws NoSuchFieldException, IllegalAccessException {
            environmentVars.remove("LPVS_GITHUB_TOKEN");
            lpvsGitHubConnectionService.setGithubTokenFromEnv();
            Field field =
                    lpvsGitHubConnectionService.getClass().getDeclaredField("GITHUB_AUTH_TOKEN");
            field.setAccessible(true);
            String githubRealTokenValue = (String) field.get(lpvsGitHubConnectionService);
            assertEquals(GH_AUTH_TOKEN, githubRealTokenValue);
        }
    }

    @Nested
    class TestCheckMethod {

        @BeforeEach
        public void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        @Test
        public void testCheckEmpty()
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

            environmentVars.set("LPVS_GITHUB_TOKEN", "");

            LPVSPullRequestRepository mocked_pullRequestRepository =
                    mock(LPVSPullRequestRepository.class);
            LPVSDetectedLicenseRepository mocked_lpvsDetectedLicenseRepository =
                    mock(LPVSDetectedLicenseRepository.class);
            LPVSLicenseRepository mocked_lpvsLicenseRepository = mock(LPVSLicenseRepository.class);
            LPVSLicenseConflictRepository mocked_lpvsLicenseConflictRepository =
                    mock(LPVSLicenseConflictRepository.class);
            LPVSExitHandler exitHandler = mock(LPVSExitHandler.class);
            LPVSGitHubConnectionService lpvsGitHubConnectionService =
                    new LPVSGitHubConnectionService("", "", "", exitHandler);

            final LPVSGitHubService gh_service =
                    new LPVSGitHubService(
                            mocked_pullRequestRepository,
                            mocked_lpvsDetectedLicenseRepository,
                            mocked_lpvsLicenseRepository,
                            mocked_lpvsLicenseConflictRepository,
                            lpvsGitHubConnectionService);
            Method method = lpvsGitHubConnectionService.getClass().getDeclaredMethod("checks");
            method.setAccessible(true);
            method.invoke(lpvsGitHubConnectionService);

            verify(exitHandler).exit(-1);
        }

        @Test
        public void testCheckNotEmpty()
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            String GH_LOGIN = "";
            String GH_AUTH_TOKEN = "non-empty";
            String GH_API_URL = "";
            LPVSExitHandler exitHandler = mock(LPVSExitHandler.class);
            LPVSGitHubConnectionService lpvsGitHubConnectionService =
                    new LPVSGitHubConnectionService(
                            GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL, exitHandler);
            Method method = lpvsGitHubConnectionService.getClass().getDeclaredMethod("checks");
            method.setAccessible(true);
            method.invoke(lpvsGitHubConnectionService);

            verify(exitHandler, never()).exit(anyInt());
        }
    }

    @Nested
    class getInternalQueueByPullRequests {

        @Test
        void testGetInternalQueueByPullRequestWithNull() {
            LPVSQueue result = gitHubService.getInternalQueueByPullRequest(null);
            assertNull(result);
        }

        @Test
        void testGetInternalQueueByPullRequest() throws IOException {
            String pullRequest = "github/owner/repo/branch/123";

            when(gitHubConnectionService.connectToGitHubApi()).thenReturn(gitHub);
            when(gitHub.getRepository("owner/repo")).thenReturn(ghRepository);
            when(ghRepository.getPullRequest(123)).thenReturn(ghPullRequest);

            LPVSQueue result = gitHubService.getInternalQueueByPullRequest(pullRequest);

            assertNotNull(result);
            assertEquals(result.getUserId(), "Single scan run");
        }

        @Test
        void testGetInternalQueueByPullRequestError() throws IOException {
            String pullRequest = "github/owner/repo/branch/123";

            when(gitHubConnectionService.connectToGitHubApi()).thenThrow(IOException.class);

            try {
                LPVSQueue result = gitHubService.getInternalQueueByPullRequest(pullRequest);
                assertNull(result, "Expected result to be null");
            } catch (Exception e) {
                fail("Exception not expected to be thrown here");
            }
        }
    }
}
