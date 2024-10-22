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
}
