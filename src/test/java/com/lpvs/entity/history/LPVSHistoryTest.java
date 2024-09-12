/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LPVSHistoryTest {

    private LPVSHistory lpvsHistory;
    private String scanDate = "2023-10-12";
    private String repositoryName = "example/repository";
    private Long pullRequestId = 1L;
    private String url = "https://example.com/pull/1";
    private String status = "Success";
    private String sender = "john.doe";
    private String pullNumber = "pull/1";
    private Boolean hasIssue = true;

    @BeforeEach
    public void setUp() {
        lpvsHistory =
                new LPVSHistory(
                        scanDate,
                        repositoryName,
                        pullRequestId,
                        url,
                        status,
                        sender,
                        pullNumber,
                        hasIssue);
    }

    @Test
    public void testGetters() {
        // Test getters
        assertEquals("2023-10-12", lpvsHistory.getScanDate());
        assertEquals("example/repository", lpvsHistory.getRepositoryName());
        assertEquals(1L, lpvsHistory.getPullRequestId().longValue());
        assertEquals("https://example.com/pull/1", lpvsHistory.getUrl());
        assertEquals("Success", lpvsHistory.getStatus());
        assertEquals("john.doe", lpvsHistory.getSender());
        assertEquals("pull/1", lpvsHistory.getPullNumber());
        assertTrue(lpvsHistory.getHasIssue());
    }

    @Test
    public void testInequality() {
        // Test inequality when objects are not the same
        LPVSHistory history1 =
                new LPVSHistory(
                        "2023-10-12",
                        "example/repository",
                        1L,
                        "https://example.com/pull/1",
                        "Success",
                        "john.doe",
                        "pull/1",
                        true);
        LPVSHistory history2 =
                new LPVSHistory(
                        "2023-10-13",
                        "another/repository",
                        2L,
                        "https://example.com/pull/2",
                        "Failure",
                        "jane.doe",
                        "pull/2",
                        false);
        assertFalse(history1.equals(history2));
        assertFalse(history2.equals(history1));
    }
}
