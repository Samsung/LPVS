/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import static org.mockito.Mockito.*;

public class LPVSFileUtilTest {

    @Test
    public void testSaveGithubDiffs() {
        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setHeadCommitSHA("aaaa");
        webhookConfig.setRepositoryUrl("http://test.com/test/test");
        ReflectionTestUtils.setField(detail, "filename", "I_am_a_file");
        LPVSFileUtil.saveGithubDiffs(
                new ArrayList<GHPullRequestFileDetail>() {
                    {
                        add(detail);
                    }
                },
                webhookConfig);
        ReflectionTestUtils.setField(detail, "patch", "+ a\n- b\n@@ -8,7 +8,6 @@\n c");
        Assertions.assertFalse(
                LPVSFileUtil.saveGithubDiffs(
                                new ArrayList<GHPullRequestFileDetail>() {
                                    {
                                        add(detail);
                                    }
                                },
                                webhookConfig)
                        .contains("Projects//aaaa"));
    }

    @Test
    public void testSaveGithubDiffsFileNameWithSlash() {
        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setHeadCommitSHA("aaaa");
        webhookConfig.setRepositoryUrl("http://test.com/test/test");
        ReflectionTestUtils.setField(detail, "filename", "dir/I_am_a_file");
        LPVSFileUtil.saveGithubDiffs(
                new ArrayList<GHPullRequestFileDetail>() {
                    {
                        add(detail);
                    }
                },
                webhookConfig);

        ReflectionTestUtils.setField(detail, "patch", "+ a\n- b\n@@ -8,7 +8,6 @@\n c");
        Assertions.assertFalse(
                LPVSFileUtil.saveGithubDiffs(
                                new ArrayList<GHPullRequestFileDetail>() {
                                    {
                                        add(detail);
                                    }
                                },
                                webhookConfig)
                        .contains("Projects//aaaa"));
    }

    @Test
    public void testSaveGithubDiffsEmptyPatchLines() {
        GHPullRequestFileDetail detail = new GHPullRequestFileDetail();
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setHeadCommitSHA("aaaa");
        webhookConfig.setRepositoryUrl("http://test.com/test/test");
        ReflectionTestUtils.setField(detail, "filename", "I_am_a_file");
        LPVSFileUtil.saveGithubDiffs(
                new ArrayList<GHPullRequestFileDetail>() {
                    {
                        add(detail);
                    }
                },
                webhookConfig);
        ReflectionTestUtils.setField(detail, "patch", "");
        Assertions.assertFalse(
                LPVSFileUtil.saveGithubDiffs(
                                new ArrayList<GHPullRequestFileDetail>() {
                                    {
                                        add(detail);
                                    }
                                },
                                webhookConfig)
                        .contains("Projects//aaaa"));
    }

    @Test
    public void testGetLocalDirectoryPathWithHeadCommitSHA() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(mockWebhookConfig.getHeadCommitSHA()).thenReturn("abcdef123");

        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");

            String result = LPVSFileUtil.getLocalDirectoryPath(mockWebhookConfig);
            String expectedPath = System.getProperty("user.home") + "/Projects/repoName/abcdef123";
            assert (result.equals(expectedPath));
        }
    }

    @Test
    public void testGetLocalDirectoryPathWithHeadCommitSHAEmpty() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(mockWebhookConfig.getHeadCommitSHA()).thenReturn("");

        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getPullRequestId(Mockito.any()))
                    .thenReturn("1");

            String result = LPVSFileUtil.getLocalDirectoryPath(mockWebhookConfig);
            String expectedPath = System.getProperty("user.home") + "/Projects/repoName/1";
            assert (result.equals(expectedPath));
        }
    }

    @Test
    public void testGetLocalDirectoryPathWithoutHeadCommitSHA() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(mockWebhookConfig.getHeadCommitSHA()).thenReturn(null);

        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getPullRequestId(Mockito.any()))
                    .thenReturn("pullRequestId");

            String result = LPVSFileUtil.getLocalDirectoryPath(mockWebhookConfig);
            String expectedPath =
                    System.getProperty("user.home") + "/Projects/repoName/pullRequestId";
            assert (result.equals(expectedPath));
        }
    }

    @Test
    public void testGetScanResultsJsonFilePathWithHeadCommitSHA() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(mockWebhookConfig.getHeadCommitSHA()).thenReturn("abcdef123");

        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");

            String result = LPVSFileUtil.getScanResultsJsonFilePath(mockWebhookConfig);
            String expectedPath =
                    System.getProperty("user.home") + "/Results/repoName/abcdef123.json";
            assert (result.equals(expectedPath));
        }
    }

    @Test
    public void testGetScanResultsJsonFilePathWithHeadCommitSHAEmpty() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(mockWebhookConfig.getHeadCommitSHA()).thenReturn("");

        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getPullRequestId(Mockito.any()))
                    .thenReturn("1");

            String result = LPVSFileUtil.getScanResultsJsonFilePath(mockWebhookConfig);
            String expectedPath = System.getProperty("user.home") + "/Results/repoName/1.json";
            assert (result.equals(expectedPath));
        }
    }

    @Test
    public void testGetScanResultsJsonFilePathWithoutHeadCommitSHA() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        Mockito.when(mockWebhookConfig.getHeadCommitSHA()).thenReturn(null);

        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getPullRequestId(Mockito.any()))
                    .thenReturn("pullRequestId");

            String result = LPVSFileUtil.getScanResultsJsonFilePath(mockWebhookConfig);
            String expectedPath =
                    System.getProperty("user.home") + "/Results/repoName/pullRequestId.json";
            assert (result.equals(expectedPath));
        }
    }

    @Test
    public void testGetScanResultsDirectoryPath() {
        LPVSQueue mockWebhookConfig = Mockito.mock(LPVSQueue.class);
        try (MockedStatic<LPVSWebhookUtil> mocked_static_file_util =
                mockStatic(LPVSWebhookUtil.class)) {
            mocked_static_file_util
                    .when(() -> LPVSWebhookUtil.getRepositoryName(Mockito.any()))
                    .thenReturn("repoName");
            String result = LPVSFileUtil.getScanResultsDirectoryPath(mockWebhookConfig);
            String expectedPath = System.getProperty("user.home") + "/Results/repoName";
            assert (result.equals(expectedPath));
        }
    }
}
