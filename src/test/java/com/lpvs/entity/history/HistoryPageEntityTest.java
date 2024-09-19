/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.history;

import com.lpvs.entity.LPVSPullRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HistoryPageEntityTest {

    private HistoryPageEntity historyPageEntity;
    private List<LPVSPullRequest> pullRequests = new ArrayList<>();
    private Page<LPVSPullRequest> prPage;
    private Long count = 2L;

    @BeforeEach
    public void setUp() {
        pullRequests.add(
                new LPVSPullRequest(
                        1L, null, null, "Title 1", null, null, null, null, null, null, null));
        pullRequests.add(
                new LPVSPullRequest(
                        2L, null, null, "Title 2", null, null, null, null, null, null, null));
        prPage = new PageImpl<>(pullRequests);
        historyPageEntity = new HistoryPageEntity(prPage, count);
    }

    @Test
    public void testGetters() {
        // Test getters
        assertEquals(2, historyPageEntity.getPrPage().getSize());
        assertEquals(2L, historyPageEntity.getCount().longValue());
    }

    @Test
    public void testInequality() {
        // Test inequality when objects are not the same
        List<LPVSPullRequest> pullRequests1 = new ArrayList<>();
        pullRequests1.add(
                new LPVSPullRequest(
                        1L, null, null, "Title 1", null, null, null, null, null, null, null));
        pullRequests1.add(
                new LPVSPullRequest(
                        2L, null, null, "Title 2", null, null, null, null, null, null, null));
        Page<LPVSPullRequest> prPage1 = new PageImpl<>(pullRequests1);
        Long count1 = 2L;
        HistoryPageEntity entity1 = new HistoryPageEntity(prPage1, count1);
        List<LPVSPullRequest> pullRequests2 = new ArrayList<>();
        pullRequests2.add(
                new LPVSPullRequest(
                        3L, null, null, "Title 3", null, null, null, null, null, null, null));
        Page<LPVSPullRequest> prPage2 = new PageImpl<>(pullRequests2);
        Long count2 = 1L;
        HistoryPageEntity entity2 = new HistoryPageEntity(prPage2, count2);
        assertFalse(entity1.equals(entity2));
        assertFalse(entity2.equals(entity1));
    }
}
