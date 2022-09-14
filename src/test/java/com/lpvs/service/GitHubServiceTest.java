/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.entity.enums.PullRequestAction;
import com.lpvs.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.*;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

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
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenThrow(new IOException("Test exception for TestGetPullRequestFiles__ApiUrlAbsentPullExceptionNoRescan"));
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
                when(mocked_repo.getPullRequests(GHIssueState.OPEN)).thenThrow(new IOException("Test exception for TestGetPullRequestFiles__ApiUrlPresentPullExceptionNoRescan"));
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
                LOG.error("TestGetPullRequestFiles__ApiUrlAbsentPullPresentNoRescan.setUp() error " + e);
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


// GHCommitPointer mocked_commit_pointer = mock(GHCommitPointer.class);

//            when(mocked_pr_2.getHead()).thenReturn(mocked_commit_pointer);
//            when(mocked_commit_pointer.getSha()).thenReturn("895337e89ae103ff2d18c9e0d93709f743226afa");
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
}
