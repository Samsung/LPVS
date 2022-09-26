/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.entity.enums.PullRequestAction;
import com.lpvs.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * todo: decide if we need extra-dependency Junit-pioneer to mock System.getenv(),
 *  and then possibly add test case for
 *  `if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();`
 *  https://stackoverflow.com/a/59635733/8463690
 */
public class GitHubServiceTest {
    private static Logger LOG = LoggerFactory.getLogger(GitHubServiceTest.class);

    /**
     * Helper class to mock `GHPullRequest`, because we cannot mock it via Mockito.
     * There is exception when we try to mock `getUrl()` method:
     *
     *    org.mockito.exceptions.misusing.WrongTypeOfReturnValue:
     *    URL cannot be returned by getUrl()
     *    getUrl() should return String
     */
    static class GHPullRequestOurMock extends GHPullRequest {
        private final URL mockedGetUrl;
        private final String mockedGetTitle;
        private final PagedIterable<GHPullRequestFileDetail> mockedListFiles;
        private final int mockedGetDeletions;
        private final GHCommitPointer mockedGetHead;

        public GHPullRequestOurMock(URL mockedGetUrl,
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
        public URL getUrl() { return mockedGetUrl; }

        @Override
        public String getTitle() { return mockedGetTitle; }

        @Override
        public PagedIterable<GHPullRequestFileDetail> listFiles() { return mockedListFiles; }

        @Override
        public int getDeletions() { return mockedGetDeletions; }

        @Override
        public GHCommitPointer getHead() { return mockedGetHead; }

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
            LOG.error(expected_arg.replace("\n", "\\n"));
            LOG.error(comment_calls.get(0).arg.replace("\n", "\\n"));

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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files = new PagedIterable<GHPullRequestFileDetail>() {
            @Override
            public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                return null;
            }
        };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setAction(PullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(
                        new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(
                        new URL(url_pr_2), "GithubService::getRepositoryLicense tests", mocked_list_files, 0, null);
            } catch (MalformedURLException e) {
                LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class); MockedStatic<FileUtil> mocked_static_file_util = mockStatic(FileUtil.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);
                mocked_static_file_util.when(() -> FileUtil.saveFiles(mocked_list_files, repo_org + "/" + repo_name, commit_sha, 0))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> FileUtil.saveFiles(mocked_list_files, repo_org + "/" + repo_name, commit_sha, 0),
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files = new PagedIterable<GHPullRequestFileDetail>() {
            @Override
            public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                return null;
            }
        };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setAction(PullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(
                        new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(
                        new URL(url_pr_2), "GithubService::getRepositoryLicense tests", mocked_list_files, 0, null);
            } catch (MalformedURLException e) {
                LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class); MockedStatic<FileUtil> mocked_static_file_util = mockStatic(FileUtil.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);
                mocked_static_file_util.when(() -> FileUtil.saveFiles(mocked_list_files, repo_org + "/" + repo_name, commit_sha, 0))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullPresentNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> FileUtil.saveFiles(mocked_list_files, repo_org + "/" + repo_name, commit_sha, 0),
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);

        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String url_pr_3 = "https://api.github.com/repos/Samsung/LPVS/pulls/20";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setAction(PullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullAbsentNoRescan() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);

        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String url_pr_3 = "https://api.github.com/repos/Samsung/LPVS/pulls/20";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setAction(PullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan.testGetPullRequestFiles__ApiUrlPresentPullAbsentNoRescan() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        final String url_pr_3 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setAction(PullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }

            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenThrow(new IOException("Test exception for TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan. Normal behavior."));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        final String url_pr_3 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setAction(PullRequestAction.OPEN);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenThrow(new IOException("Test exception for TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan. Normal behavior."));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertNull(gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan.testGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        PagedIterable<GHPullRequestFileDetail> mocked_list_files = new PagedIterable<GHPullRequestFileDetail>() {
            @Override
            public PagedIterator<GHPullRequestFileDetail> _iterator(int i) {
                return null;
            }
        };
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        GHCommitPointer mocked_commit_pointer = mock(GHCommitPointer.class);
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String githubFiles = "Projects/Samsung/LPVS/895337e89ae103ff2d18c9e0d93709f743226afa";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setPullRequestId(19L);
            webhookConfig.setAction(PullRequestAction.RESCAN);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(
                        new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(
                        new URL(url_pr_2), "GithubService::getRepositoryLicense tests", mocked_list_files, 0, mocked_commit_pointer);
            } catch (MalformedURLException e) {
                LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }

            when(mocked_commit_pointer.getSha()).thenReturn(commit_sha);
        }

        @Test
        public void testGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class); MockedStatic<FileUtil> mocked_static_file_util = mockStatic(FileUtil.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);
                mocked_static_file_util.when(() -> FileUtil.saveFiles(mocked_list_files, repo_org + "/" + repo_name, commit_sha, 0))
                        .thenReturn(githubFiles);

                // main test
                assertEquals(githubFiles, gh_service.getPullRequestFiles(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent.testGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                } catch (IOException e) {
                    LOG.error("TestGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent.testGetPullRequestFiles__ApiUrlPresentPullPresentRescanPresent() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_repo);

                verify(mocked_commit_pointer, times(1)).getSha();
                verifyNoMoreInteractions(mocked_commit_pointer);

                // `mocked_static_file_util` verify
                mocked_static_file_util.verify(
                        () -> FileUtil.saveFiles(mocked_list_files, repo_org + "/" + repo_name, commit_sha, 0),
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setHeadCommitSHA(head_commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(head_commit_sha, GHCommitState.PENDING, null,
                        "Scanning opensource licenses", "[Open Source License Validation]")).thenReturn(null);
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetPendingCheck__ApiUrlAbsentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestSetPendingCheck__ApiUrlAbsentNormalExecution.testSetPendingCheck__ApiUrlAbsentNormalExecution() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).createCommitStatus(
                            head_commit_sha, GHCommitState.PENDING, null,
                            "Scanning opensource licenses", "[Open Source License Validation]");
                } catch (IOException e) {
                    LOG.error("TestSetPendingCheck__ApiUrlAbsentNormalExecution.testSetPendingCheck__ApiUrlAbsentNormalExecution() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setHeadCommitSHA(head_commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(head_commit_sha, GHCommitState.PENDING, null,
                        "Scanning opensource licenses", "[Open Source License Validation]")).thenReturn(null);
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetPendingCheck__ApiUrlPresentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestSetPendingCheck__ApiUrlPresentNormalExecution.testSetPendingCheck__ApiUrlPresentNormalExecution() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).createCommitStatus(
                            head_commit_sha, GHCommitState.PENDING, null,
                            "Scanning opensource licenses", "[Open Source License Validation]");
                } catch (IOException e) {
                    LOG.error("TestSetPendingCheck__ApiUrlPresentNormalExecution.testSetPendingCheck__ApiUrlPresentNormalExecution() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
        }

        @Test
        public void testSetPendingCheck__ApiUrlAbsentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenThrow(new IOException("Test exception for TestSetPendingCheck__ApiUrlAbsentCantAuthorize. Normal behavior."));

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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
        }

        @Test
        public void testSetPendingCheck__ApiUrlPresentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenThrow(new IOException("Test exception for TestSetPendingCheck__ApiUrlPresentCantAuthorize. Normal behavior."));

                // main test
                gh_service.setPendingCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestSetErrorCheck__ApiUrlAbsentNormalExecution {
        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "";
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setHeadCommitSHA(head_commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(head_commit_sha, GHCommitState.ERROR, null,
                        "Scanning process failed", "[Open Source License Validation]")).thenReturn(null);
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetErrorCheck__ApiUrlAbsentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestSetErrorCheck__ApiUrlAbsentNormalExecution.testSetErrorCheck__ApiUrlAbsentNormalExecution() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).createCommitStatus(
                            head_commit_sha, GHCommitState.ERROR, null,
                            "Scanning process failed", "[Open Source License Validation]");
                } catch (IOException e) {
                    LOG.error("TestSetErrorCheck__ApiUrlAbsentNormalExecution.testSetErrorCheck__ApiUrlAbsentNormalExecution() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String head_commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setHeadCommitSHA(head_commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }

            try {
                // default behavior, it is written here just for clarity
                when(mocked_repo.createCommitStatus(head_commit_sha, GHCommitState.ERROR, null,
                        "Scanning process failed", "[Open Source License Validation]")).thenReturn(null);
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testSetErrorCheck__ApiUrlPresentNormalExecution() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("TestSetErrorCheck__ApiUrlPresentNormalExecution.testSetErrorCheck__ApiUrlPresentNormalExecution() error " + e);
                    fail();
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).createCommitStatus(
                            head_commit_sha, GHCommitState.ERROR, null,
                            "Scanning process failed", "[Open Source License Validation]");
                } catch (IOException e) {
                    LOG.error("TestSetErrorCheck__ApiUrlPresentNormalExecution.testSetErrorCheck__ApiUrlPresentNormalExecution() error " + e);
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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
        }

        @Test
        public void testSetErrorCheck__ApiUrlAbsentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenThrow(new IOException("Test exception for TestSetErrorCheck__ApiUrlAbsentCantAuthorize. Normal behavior."));

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
        final GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
        }

        @Test
        public void testSetErrorCheck__ApiUrlPresentCantAuthorize() {
            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenThrow(new IOException("Test exception for TestSetErrorCheck__ApiUrlPresentCantAuthorize. Normal behavior."));

                // main test
                gh_service.setErrorCheck(webhookConfig);

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();
            }
        }
    }

    @Nested
    class TestCommentResults__PrAbsent {
        final GitHubService gh_service = new GitHubService(null, null, null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String url_pr_3 = "https://api.github.com/repos/Samsung/LPVS/pulls/20";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = GitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error("TestCommentResults__PrAbsent.setUp() error " + e);
                fail();
            }

            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setPullRequestAPIUrl(url_pr_3);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                LOG.error("TestCommentResults__PrAbsent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testCommentResults__PrAbsent() {
            // main test
            gh_service.commentResults(webhookConfig, null, null);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
            } catch (IOException e) {
                LOG.error("TestCommentResults__PrAbsent.testCommentResults__PrAbsent() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
            } catch (IOException e) {
                LOG.error("TestCommentResults__PrAbsent.testCommentResults__PrAbsent() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);
        }
    }

    @Nested
    class TestCommentResults__CantAuthorize {
        final GitHubService gh_service = new GitHubService(null, null, null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = GitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error("TestCommentResults__CantAuthorize.setUp() error " + e);
                fail();
            }

            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);

            try {
                when(mocked_instance_gh
                        .getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName()))
                        .thenThrow(new IOException("Test exception for TestCommentResults__CantAuthorize. Normal behavior."));
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
        }

        @Test
        public void testCommentResults__CantAuthorize() {
            // main test
            gh_service.commentResults(webhookConfig, null, null);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
            } catch (IOException e) {
                LOG.error("TestCommentResults__CantAuthorize.testCommentResults__CantAuthorize() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);
        }
    }

    @Nested
    class TestCommentResults__ScanResultsEmpty {
        final GitHubService gh_service = new GitHubService(null, null, null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = GitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error("TestCommentResults__ScanResultsEmpty.setUp() error " + e);
                fail();
            }

            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                LOG.error("TestCommentResults__ScanResultsEmpty.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }
        }

        @Test
        public void testCommentResults__ScanResultsEmpty() {
            // main test
            gh_service.commentResults(webhookConfig, List.of(), null);

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
            } catch (IOException e) {
                LOG.error("TestCommentResults__ScanResultsEmpty.testCommentResults__ScanResultsEmpty() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1)).createCommitStatus(commit_sha, GHCommitState.SUCCESS, null,
                        "No license issue detected", "[Open Source License Validation]");
            } catch (IOException e) {
                LOG.error("TestCommentResults__ScanResultsEmpty.testCommentResults__ScanResultsEmpty() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_repo);
        }
    }

    @Nested
    class TestCommentResults__ProhibitedPresentConflictsPresent {
        final GitHubService gh_service = new GitHubService(null, null, null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";

        // `webhookConfig`
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String repository_url = "https://github.com/Samsung/LPVS";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 = "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/GitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/GitHubService.java";
        final String snippet_match_1 = "/**\n" +
                " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n" +
                " *\n" +
                " * Use of this source code is governed by a MIT license that can be\n" +
                " * found in the LICENSE file.\n" +
                " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String access_1 = "PROHIBITED";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        // `conflict_1`
        LicenseService.Conflict<String, String> conflict_1;
        final String conflict_1_l1 = "MIT";
        final String conflict_1_l2 = "Apache-1.0";

        final String expected_comment = "**\\[Open Source License Validation\\]** Potential license problem(s) detected \n\n" +
                "**Detected licenses:**\n\n\n" +
                "**File:** src/main/java/com/lpvs/service/GitHubService.java\n" +
                "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> (prohibited)\n" +
                "**Component:** LPVS::Services (https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/GitHubService.java)\n" +
                "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/GitHubService.java#L1L6\">1-6</a>\n" +
                "**Snippet Match:** /**\n" +
                " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n" +
                " *\n" +
                " * Use of this source code is governed by a MIT license that can be\n" +
                " * found in the LICENSE file.\n" +
                " */\n\n\n\n\n" +
                "**Detected license conflicts:**\n\n\n" +
                "<ul><li>MIT and Apache-1.0</li></ul>\n";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = GitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error("TestCommentResults__ProhibitedPresentConflictsPresent.setUp() error " + e);
                fail();
            }

            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setRepositoryUrl(repository_url);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                LOG.error("TestCommentResults__ProhibitedPresentConflictsPresent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 = new LPVSLicense(1L, license_name_1, spdx_id_1, access_1, checklist_url_1, List.of());
            lpvs_file_1 = new LPVSFile(1L, file_url_1, file_path_1, snippet_match_1, matched_lines_1, Set.of(lpvs_license_1), component_1);
            conflict_1 = new LicenseService.Conflict<>(conflict_1_l1, conflict_1_l2);
        }

        @Test
        public void testCommentResults__ProhibitedPresentConflictsPresent() {
            // main test
            gh_service.commentResults(webhookConfig, List.of(lpvs_file_1), List.of(conflict_1));

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
            } catch (IOException e) {
                LOG.error("TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1)).createCommitStatus(commit_sha, GHCommitState.FAILURE, null,
                        "Potential license problem(s) detected", "[Open Source License Validation]");
            } catch (IOException e) {
                LOG.error("TestCommentResults__ProhibitedPresentConflictsPresent.testCommentResults__ProhibitedPresentConflictsPresent() error " + e);
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
        final GitHubService gh_service = new GitHubService(null, null, null);
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHPullRequest mocked_pr_1;
        GHPullRequest mocked_pr_2;
        final String url_pr_1 = "https://api.github.com/repos/Samsung/LPVS/pulls/18";
        final String url_pr_2 = "https://api.github.com/repos/Samsung/LPVS/pulls/19";

        // `webhookConfig`
        WebhookConfig webhookConfig;
        final String repo_org = "Samsung";
        final String repo_name = "LPVS";
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String repository_url = "https://github.com/Samsung/LPVS";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_url_1 = "https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/GitHubService.java";
        final String file_path_1 = "src/main/java/com/lpvs/service/GitHubService.java";
        final String snippet_match_1 = "/**\n" +
                " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n" +
                " *\n" +
                " * Use of this source code is governed by a MIT license that can be\n" +
                " * found in the LICENSE file.\n" +
                " */\n";
        final String matched_lines_1 = "1-6";
        final String component_1 = "LPVS::Services";

        // `lpvs_license_1`
        LPVSLicense lpvs_license_1;
        final String license_name_1 = "MIT License";
        final String spdx_id_1 = "MIT";
        final String access_1 = "PERMITTED";
        final String checklist_url_1 = "https://opensource.org/licenses/MIT";

        final String expected_comment = "**\\[Open Source License Validation\\]**  No license issue detected \n\n" +
                "**Detected licenses:**\n\n\n" +
                "**File:** src/main/java/com/lpvs/service/GitHubService.java\n" +
                "**License(s):** <a target=\"_blank\" href=\"https://opensource.org/licenses/MIT\">MIT</a> (permitted)\n" +
                "**Component:** LPVS::Services (https://github.com/Samsung/LPVS/tree/main/src/main/java/com/lpvs/service/GitHubService.java)\n" +
                "**Matched Lines:** <a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/GitHubService.java#L1L6\">1-6</a>\n" +
                "**Snippet Match:** /**\n" +
                " * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.\n" +
                " *\n" +
                " * Use of this source code is governed by a MIT license that can be\n" +
                " * found in the LICENSE file.\n" +
                " */\n\n\n\n\n\n";

        @BeforeEach
        void setUp() {
            // set `private static GitHub gitHub;` value
            try {
                Field staticPrivateGithub = GitHubService.class.getDeclaredField("gitHub");
                staticPrivateGithub.setAccessible(true);
                staticPrivateGithub.set(null, mocked_instance_gh);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error("TestCommentResults__ProhibitedAbsentConflictsAbsent.setUp() error " + e);
                fail();
            }

            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName(repo_name);
            webhookConfig.setRepositoryOrganization(repo_org);
            webhookConfig.setPullRequestAPIUrl(url_pr_2);
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setRepositoryUrl(repository_url);

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                mocked_pr_1 = new GHPullRequestOurMock(new URL(url_pr_1), null, null, -1, null);
                mocked_pr_2 = new GHPullRequestOurMock(new URL(url_pr_2), null, null, -1, null);
            } catch (MalformedURLException e) {
                LOG.error("TestCommentResults__ProhibitedAbsentConflictsAbsent.setUp() error " + e);
                fail();
            }
            try {
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenReturn(Arrays.asList(mocked_pr_1, mocked_pr_2));
            } catch (IOException e) {
                LOG.error("mocked_repo.getPullRequests error " + e);
            }

            lpvs_license_1 = new LPVSLicense(1L, license_name_1, spdx_id_1, access_1, checklist_url_1, List.of());
            lpvs_file_1 = new LPVSFile(1L, file_url_1, file_path_1, snippet_match_1, matched_lines_1, Set.of(lpvs_license_1), component_1);
        }

        @Test
        public void testCommentResults__ProhibitedAbsentConflictsAbsent() {
            // main test
            gh_service.commentResults(webhookConfig, List.of(lpvs_file_1), List.of());

            // `mocked_instance_gh` verify
            try {
                verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
            } catch (IOException e) {
                LOG.error("TestCommentResults__ProhibitedAbsentConflictsAbsent.testCommentResults__ProhibitedAbsentConflictsAbsent() error " + e);
                fail();
            }
            verifyNoMoreInteractions(mocked_instance_gh);

            // `mocked_repo` verify
            try {
                verify(mocked_repo, times(1)).getPullRequests(GHIssueState.OPEN);
                verify(mocked_repo, times(1)).createCommitStatus(commit_sha, GHCommitState.SUCCESS, null,
                        "No license issue detected", "[Open Source License Validation]");
            } catch (IOException e) {
                LOG.error("TestCommentResults__ProhibitedAbsentConflictsAbsent.testCommentResults__ProhibitedAbsentConflictsAbsent() error " + e);
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
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHLicense mocked_license = mock(GHLicense.class);
        final String test_license_key = "test_license_key";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(mocked_license);
            } catch (IOException e) {
                LOG.error("mocked_repo.getLicense error " + e);
            }
            when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlAbsentLisencePresent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertEquals(test_license_key, gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    LOG.error("mocked_repo.getLicense error " + e);
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
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        // GHLicense mocked_license = mock(GHLicense.class);
        // final String test_license_key = "test_license_key";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(null);
            } catch (IOException e) {
                LOG.error("mocked_repo.getLicense error " + e);
            }
            // when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlAbsentLisenceAbsent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    LOG.error("mocked_repo.getLicense error " + e);
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
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        GHLicense mocked_license = mock(GHLicense.class);
        final String test_license_key = "test_license_key";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(mocked_license);
            } catch (IOException e) {
                LOG.error("mocked_repo.getLicense error " + e);
            }
            when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlPresentLisencePresent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertEquals(test_license_key, gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    LOG.error("mocked_repo.getLicense error " + e);
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
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;
        GitHub mocked_instance_gh = mock(GitHub.class);
        GHRepository mocked_repo = mock(GHRepository.class);
        // GHLicense mocked_license = mock(GHLicense.class);
        // final String test_license_key = "test_license_key";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");

            try {
                when(mocked_instance_gh.getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName())).thenReturn(mocked_repo);
            } catch (IOException e) {
                LOG.error("mocked_repo.getRepository error " + e);
            }
            try {
                when(mocked_repo.getLicense()).thenReturn(null);
            } catch (IOException e) {
                LOG.error("mocked_repo.getLicense error " + e);
            }
            // when(mocked_license.getKey()).thenReturn(test_license_key);
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlPresentLisenceAbsent() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenReturn(mocked_instance_gh);

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

                // `mocked_instance_gh` verify
                try {
                    verify(mocked_instance_gh, times(1)).getRepository(webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName());
                } catch (IOException e) {
                    LOG.error("mocked_instance_gh.getRepository error " + e);
                }
                verifyNoMoreInteractions(mocked_instance_gh);

                // `mocked_repo` verify
                try {
                    verify(mocked_repo, times(1)).getLicense();
                } catch (IOException e) {
                    LOG.error("mocked_repo.getLicense error " + e);
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
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlAbsentCantAuthorize() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connect(GH_LOGIN, GH_AUTH_TOKEN)).thenThrow(new IOException("test cant authorize"));

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
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");
        }

        @Test
        public void testGetRepositoryLicense__ApiUrlPresentCantAuthorize() {

            try (MockedStatic<GitHub> mocked_static_gh = mockStatic(GitHub.class)) {
                mocked_static_gh.when(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN)).thenThrow(new IOException("test cant authorize"));

                // main test
                assertEquals("Proprietary", gh_service.getRepositoryLicense(webhookConfig));

                // verification of calling methods on `Mock`s
                // `mocked_static_gh` verify
                mocked_static_gh.verify(() -> GitHub.connectToEnterpriseWithOAuth(GH_API_URL, GH_LOGIN, GH_AUTH_TOKEN), times(1));
                mocked_static_gh.verifyNoMoreInteractions();

            }
        }
    }

    @Nested
    class TestGetMatchedLinesAsLink_NotAll {
        final GitHubService gh_service = new GitHubService(null, null, null);

        // `webhookConfig`
        WebhookConfig webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String repository_url = "https://github.com/Samsung/LPVS";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_path_1 = "src/main/java/com/lpvs/service/GitHubService.java";
        final String matched_lines_1 = "1-6";

        final String expected_result = "<a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/src/main/java/com/lpvs/service/GitHubService.java#L1L6\">1-6</a>";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setRepositoryUrl(repository_url);

            lpvs_file_1 = new LPVSFile(1L, null, file_path_1, null, matched_lines_1, null, null);
        }

        @Test
        public void testGetMatchedLinesAsLink_NotAll() {
            // main test
            assertEquals(expected_result, gh_service.getMatchedLinesAsLink(webhookConfig, lpvs_file_1));
        }
    }

    @Nested
    class TestGetMatchedLinesAsLink_All {
        final GitHubService gh_service = new GitHubService(null, null, null);

        // `webhookConfig`
        WebhookConfig webhookConfig;
        final String commit_sha = "895337e89ae103ff2d18c9e0d93709f743226afa";
        final String repository_url = "https://github.com/Samsung/LPVS";

        // `lpvs_file_1`
        LPVSFile lpvs_file_1;
        final String file_path_1 = "LICENSE";
        final String matched_lines_1 = "all";

        final String expected_result = "<a target=\"_blank\" href=\"https://github.com/Samsung/LPVS/blob/895337e89ae103ff2d18c9e0d93709f743226afa/LICENSE\">all</a>";

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setHeadCommitSHA(commit_sha);
            webhookConfig.setRepositoryUrl(repository_url);

            lpvs_file_1 = new LPVSFile(1L, null, file_path_1, null, matched_lines_1, null, null);
        }

        @Test
        public void testGetMatchedLinesAsLink_All() {
            // main test
            assertEquals(expected_result, gh_service.getMatchedLinesAsLink(webhookConfig, lpvs_file_1));
        }
    }
    
    @Nested
    class TestCommentResults {

        final String GH_LOGIN = "test_login";
        final String GH_AUTH_TOKEN = "test_auth_token";
        final String GH_API_URL = "test_api_url";
        GitHubService gh_service = new GitHubService(GH_LOGIN, GH_AUTH_TOKEN, GH_API_URL);
        WebhookConfig webhookConfig;

        @BeforeEach
        void setUp() {
            webhookConfig = new WebhookConfig();
            webhookConfig.setRepositoryName("LPVS");
            webhookConfig.setRepositoryOrganization("Samsung");
            webhookConfig.setPullRequestAPIUrl("http://url.com");
        }

        @Test
        public void testCommentResults() throws IOException {
            GitHub gitHub = Mockito.mock(GitHub.class);
            GHRepository repository = Mockito.mock(GHRepository.class);
            ReflectionTestUtils.setField(gh_service, "gitHub", gitHub);
            Mockito.when(gitHub.getRepository(
                    webhookConfig.getRepositoryOrganization() + "/" + webhookConfig.getRepositoryName()))
                    .thenReturn(repository);
            LPVSFile file = new LPVSFile();
            LPVSLicense license = new LPVSLicense(){{
                setChecklist_url("");
                setAccess("unrviewed");
            }};
            file.setLicenses(new HashSet<LPVSLicense>(){{
                add(license);
            }});
            file.setFilePath("");
            file.setComponent("");
            file.setSnippetMatch("");
            file.setMatchedLines("");
            List<LPVSFile> fileList = new ArrayList<LPVSFile>(){{
                add(file);
            }};
            List<LicenseService.Conflict<String, String>> conflictList = new ArrayList<>();
            conflictList.add(new LicenseService.Conflict<>("1", "2"));
            GHPullRequest pullRequest = new GHPullRequest();
            ReflectionTestUtils.setField(pullRequest, "url", "http://url.com");
            List<GHPullRequest> pullRequestList = new ArrayList<GHPullRequest>(){{
                add(pullRequest);
            }};
            Mockito.when(repository.getPullRequests(GHIssueState.OPEN))
                    .thenReturn(pullRequestList);
            gh_service.commentResults(webhookConfig, fileList, conflictList);
            license.setAccess("");
            gh_service.commentResults(webhookConfig, fileList, conflictList);
            Mockito.verify(gitHub, times(2)).getRepository(Mockito.anyString());
        }
    }
}
