/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestAction;
import com.lpvs.repository.LPVSQueueRepository;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
public class LPVSQueueServiceTest {

    @Nested
    class TestQueueMethods {

        LPVSQueueService queueService;

        LPVSQueue whConfig1;
        LPVSQueue whConfig2;
        LPVSQueue whConfig3;
        LPVSQueue whConfig4;
        LPVSQueue whConfig5;
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);

        @BeforeEach
        void setUp() {
            queueService = new LPVSQueueService(mocked_queueRepository);

            whConfig1 = new LPVSQueue();
            whConfig1.setId(1L);

            whConfig2 = new LPVSQueue();
            whConfig2.setId(2L);

            whConfig3 = new LPVSQueue();
            whConfig3.setId(3L);

            whConfig4 = new LPVSQueue();
            whConfig4.setId(4L);

            whConfig5 = new LPVSQueue();
            whConfig5.setId(5L);
        }

        @Test
        public void testQueueMethods() {
            try {
                queueService.addFirst(whConfig1);
                queueService.addFirst(whConfig2);
                queueService.addFirst(whConfig3);
                queueService.addFirst(whConfig4);
                queueService.addFirst(whConfig5);

                assertEquals(whConfig5, queueService.getQueue().take());

                // `whConfig`s 1-4 are left in Queue
                queueService.delete(whConfig4);
                assertEquals(whConfig3, queueService.getQueue().take());

            } catch (InterruptedException e) {
                log.error("InterruptedException at LPVSQueueServiceTest.testQueueMethods(): " + e);
                fail();
            }
        }
    }

    @Nested
    class TestProcessWebHook__queueMethods {

        LPVSQueueService queueService;
        LPVSQueueRepository mocked_queueRepository = mock(LPVSQueueRepository.class);

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            queueService = new LPVSQueueService(mocked_queueRepository);
        }

        @Test
        public void testCheckForQueue() {
            LPVSQueue webhookConfig = new LPVSQueue();
            webhookConfig.setAttempts(100);
            webhookConfig.setDate(new Date());
            when(mocked_queueRepository.findAll()).thenReturn(List.of(webhookConfig));
            assertDoesNotThrow(() -> queueService.checkForQueue());
        }

        @Test
        public void testGetQueueFirstElement() throws InterruptedException {
            LPVSQueue queue = new LPVSQueue();
            queue.setId(1L);
            queue.setAttempts(4);
            queue.setAction(LPVSPullRequestAction.OPEN);
            queue.setUserId("userId");
            queue.setHeadCommitSHA("commitSha");
            queue.setPullRequestUrl("url");
            queueService.add(queue);
            LPVSQueue result = queueService.getQueueFirstElement();
            assertNotNull(result);
        }

        @Test
        public void testAddFirst() {
            LPVSQueue queue = new LPVSQueue();
            assertDoesNotThrow(() -> queueService.addFirst(queue));
        }

        @Test
        public void testAdd() {
            LPVSQueue queue = new LPVSQueue();
            assertDoesNotThrow(() -> queueService.add(queue));
        }

        @Test
        public void testDelete() {
            LPVSQueue queue = new LPVSQueue();
            queue.setId(1L);
            queue.setAttempts(4);
            queue.setAction(LPVSPullRequestAction.OPEN);
            queue.setUserId("userId");
            queue.setHeadCommitSHA("commitSha");
            queue.setPullRequestUrl("url");
            assertDoesNotThrow(() -> queueService.delete(queue));
            verify(mocked_queueRepository).deleteById(queue.getId());
        }
    }
}
