/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSVcs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

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
        Mockito.when(LPVSPayloadUtil.getRepositoryUrl(webhookConfig))
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
        Mockito.when(LPVSPayloadUtil.getRepositoryUrl(webhookConfig))
                .thenReturn("https://github.com/repo");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("headCommitSHA");
        String result = LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GITHUB);
        assertEquals(
                "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt#L1L5\">1-5</a>  "
                        + "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt#L7\">7</a>  "
                        + "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/exampleFile.txt#L9L12\">9-12</a>  ",
                result);
    }

    @Test
    public void testGetMatchedLinesAsLinkWithNonGitHubVcs() {
        LPVSFile file = new LPVSFile();
        file.setFilePath("exampleFile.txt");
        file.setMatchedLines("all");
        Mockito.when(LPVSPayloadUtil.getRepositoryUrl(webhookConfig))
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
        Mockito.when(LPVSPayloadUtil.getRepositoryUrl(webhookConfig))
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
    void testConstructorThrowsException_N() {
        try {
            Constructor<LPVSCommentUtil> constructor =
                    LPVSCommentUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (InvocationTargetException e) {
            assertInstanceOf(
                    UnsupportedOperationException.class,
                    e.getCause(),
                    "UnsupportedOperationException expected");
        } catch (Exception e) {
            fail("Unexpected exception type thrown: " + e.getCause());
        }
    }

    @Test
    public void testGetMatchedLinesAsLinkEscapesFilePath() {
        LPVSFile file = new LPVSFile();
        file.setFilePath("\"><img src=x onerror=alert(1)>");
        file.setMatchedLines("all");
        Mockito.when(LPVSPayloadUtil.getRepositoryUrl(webhookConfig))
                .thenReturn("https://github.com/repo");
        Mockito.when(webhookConfig.getHeadCommitSHA()).thenReturn("headCommitSHA");
        String result = LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GITHUB);
        assertEquals(
                "<a target=\"_blank\" href=\"https://github.com/repo/blob/headCommitSHA/"
                        + "&quot;&gt;&lt;img src=x onerror=alert(1)&gt;\">all</a>",
                result);
    }

    @Test
    public void testGetMatchedLinesAsLinkNoWebhookConfigEscapesLines() {
        LPVSFile file = new LPVSFile();
        file.setMatchedLines("<b>1-5</b>");
        assertEquals(
                "&lt;b&gt;1-5&lt;/b&gt;",
                LPVSCommentUtil.getMatchedLinesAsLink(null, file, LPVSVcs.GITHUB));
    }

    @Test
    public void testEscapeHtml() {
        assertEquals(
                "&lt;a href=&quot;x&quot;&gt;&amp;&#39;",
                LPVSCommentUtil.escapeHtml("<a href=\"x\">&'"));
        assertEquals("null", LPVSCommentUtil.escapeHtml(null));
    }

    @Test
    public void testIsSafeUrl() {
        assertTrue(LPVSCommentUtil.isSafeUrl("https://example.com"));
        assertTrue(LPVSCommentUtil.isSafeUrl(" HTTP://example.com"));
        assertFalse(LPVSCommentUtil.isSafeUrl("javascript:alert(1)"));
        assertFalse(LPVSCommentUtil.isSafeUrl("data:text/html,<script>alert(1)</script>"));
        assertFalse(LPVSCommentUtil.isSafeUrl("relative/path"));
        assertFalse(LPVSCommentUtil.isSafeUrl(null));
    }

    @Test
    public void testGetHtmlLink() {
        assertEquals(
                "<a href=\"https://example.com/?a=1&amp;b=&quot;2&quot;\">text&lt;/a&gt;</a>",
                LPVSCommentUtil.getHtmlLink(
                        "https://example.com/?a=1&b=\"2\"", "text</a>", false));
        assertEquals(
                "<a target=\"_blank\" href=\"https://example.com\">text</a>",
                LPVSCommentUtil.getHtmlLink("https://example.com", "text", true));
        assertEquals(
                "&lt;b&gt;text&lt;/b&gt;",
                LPVSCommentUtil.getHtmlLink("javascript:alert(1)", "<b>text</b>", false));
    }
}
