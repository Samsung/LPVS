/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HistoryEntityTest {

    private HistoryEntity historyEntity;
    private List<LPVSHistory> lpvsHistories = new ArrayList<>();
    private Long count = 2L;

    @BeforeEach
    public void setUp() {
        lpvsHistories.add(
                new LPVSHistory(
                        "2023-10-12",
                        "example/repository1",
                        1L,
                        "https://example.com/pull/1",
                        "Success",
                        "john.doe",
                        "pull/1",
                        true));
        lpvsHistories.add(
                new LPVSHistory(
                        "2023-10-12",
                        "example/repository2",
                        2L,
                        "https://example.com/pull/2",
                        "Success",
                        "john.doe",
                        "pull/2",
                        true));
        historyEntity = new HistoryEntity(lpvsHistories, count);
    }

    @Test
    public void testGetters() {
        // Test getters
        assertEquals(2, historyEntity.getLpvsHistories().size());
        assertEquals(2L, historyEntity.getCount().longValue());
    }

    @Test
    public void testInequality() {
        // Test inequality when objects are not the same
        List<LPVSHistory> lpvsHistories1 = new ArrayList<>();
        lpvsHistories1.add(
                new LPVSHistory(
                        "2023-10-12",
                        "example/repository1",
                        1L,
                        "https://example.com/pull/1",
                        "Success",
                        "john.doe",
                        "pull/1",
                        true));
        lpvsHistories1.add(
                new LPVSHistory(
                        "2023-10-12",
                        "example/repository2",
                        2L,
                        "https://example.com/pull/2",
                        "Success",
                        "john.doe",
                        "pull/2",
                        true));
        Long count1 = 2L;
        HistoryEntity entity1 = new HistoryEntity(lpvsHistories1, count1);
        List<LPVSHistory> lpvsHistories2 = new ArrayList<>();
        lpvsHistories2.add(
                new LPVSHistory(
                        "2023-10-12",
                        "example/repository3",
                        3L,
                        "https://example.com/pull/3",
                        "Success",
                        "john.doe",
                        "pull/3",
                        true));
        Long count2 = 1L;
        HistoryEntity entity2 = new HistoryEntity(lpvsHistories2, count2);
        assertFalse(entity1.equals(entity2));
        assertFalse(entity2.equals(entity1));
    }
}
