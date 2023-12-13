/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSVcs;
import com.lpvs.service.LPVSLicenseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LPVSCommentUtilTest {

    @Mock private LPVSQueue webhookConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMatchedLinesAsLinkAll() {
        LPVSFile file = new LPVSFile();
        file.setFilePath("exampleFile.txt");
        file.setMatchedLines("all");
        Mockito.when(LPVSWebhookUtil.getRepositoryUrl(webhookConfig))
                .thenReturn("https://github.com/repo");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("headCommitSHA");
        String result = LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GITHUB);
        assertEquals(
                "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt\">all</a>",
                result);
    }

    @Test
    public void testGetMatchedLinesAsLinkMultipleLines() {
        LPVSFile file = new LPVSFile();
        file.setFilePath("exampleFile.txt");
        file.setMatchedLines("1-5,7,9-12");
        Mockito.when(LPVSWebhookUtil.getRepositoryUrl(webhookConfig))
                .thenReturn("https://github.com/repo");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("headCommitSHA");
        String result = LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GITHUB);
        assertEquals(
                "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt#L1L5\">1-5</a>"
                        + "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt#L7\">7</a>"
                        + "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt#L9L12\">9-12</a>",
                result);
    }

    @Test
    public void testGetMatchedLinesAsLinkWithNonGitHubVcs() {
        LPVSFile file = new LPVSFile();
        file.setFilePath("exampleFile.txt");
        file.setMatchedLines("all");
        Mockito.when(LPVSWebhookUtil.getRepositoryUrl(webhookConfig))
                .thenReturn("https://gerrit.org/repo");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("headCommitSHA");
        String result = LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GERRIT);
        assertEquals("all (https://gerrit.org/repo/blob/headCommitSHA/exampleFile.txt)", result);
    }

    @Test
    public void testGetMatchedLinesAsLinkWithNonGitHubVcsMultipleLines() {
        LPVSFile file = new LPVSFile();
        file.setFilePath("exampleFile.txt");
        file.setMatchedLines("1-5,7,9-12");
        Mockito.when(LPVSWebhookUtil.getRepositoryUrl(webhookConfig))
                .thenReturn("https://gerrit.org/repo");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("headCommitSHA");
        String result = LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GERRIT);
        assertEquals(
                "1-5 (https://gerrit.org/repo/blob/headCommitSHA/exampleFile.txt#L1L5) "
                        + "7 (https://gerrit.org/repo/blob/headCommitSHA/exampleFile.txt#L7) "
                        + "9-12 (https://gerrit.org/repo/blob/headCommitSHA/exampleFile.txt#L9L12) ",
                result);
    }

    @Test
    void testReportCommentBuilder() {
        LPVSQueue webhookConfig = new LPVSQueue();
        List<LPVSFile> scanResults = new ArrayList<>();
        List<LPVSLicenseService.Conflict<String, String>> conflicts = new ArrayList<>();

        String comment =
                LPVSCommentUtil.reportCommentBuilder(webhookConfig, scanResults, conflicts);

        assertNotNull(comment);
    }

    @Test
    void testBuildHTMLComment() {
        LPVSQueue webhookConfig = new LPVSQueue();
        List<LPVSFile> scanResults = new ArrayList<>();
        List<LPVSLicenseService.Conflict<String, String>> conflicts = new ArrayList<>();

        String htmlComment =
                LPVSCommentUtil.buildHTMLComment(webhookConfig, scanResults, conflicts);

        assertNotNull(htmlComment);
    }

    @Test
    void testSaveHTMLToFile() throws IOException {
        String htmlContent = "<html><body><p>Test HTML</p></body></html>";
        String filePath = "test-output.html";

        LPVSCommentUtil.saveHTMLToFile(htmlContent, filePath);

        assertTrue(Files.exists(Paths.get(filePath)));
        String fileContent = Files.readString(Paths.get(filePath));
        assertEquals(htmlContent, fileContent);

        // Clean up: delete the created file
        // TODO: need to switch to temp folder option
        Files.deleteIfExists(Paths.get(filePath));
    }
}
