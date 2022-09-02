/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.config.WebhookConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class QueueProcessorServiceTest {
    private static Logger LOG = LoggerFactory.getLogger(QueueProcessorServiceTest.class);

    QueueProcessorService queueProcessorService;
    WebhookConfig webhookConfigTest;
    QueueService queueService;

    @BeforeEach
    void setUp() {
        webhookConfigTest = new WebhookConfig();

        queueService = mock(QueueService.class);
        try {
            when(queueService.getQueueFirstElement())
                    // first iteration
                    .thenReturn(webhookConfigTest)
                    // second iteration
                    .thenThrow(new InterruptedException("Test InterruptedException at 2nd iteration"));
        } catch (InterruptedException e) {
            LOG.error("InterruptedException at QueueProcessorServiceTest.setUp(): " + e);
            fail();
        }

        queueProcessorService = new QueueProcessorService(queueService);
    }

    @Test
    public void testQueueProcessor() {
        // first iteration of loop inside method passes, second stops by Exception
        try {
            Method method = queueProcessorService.getClass().getDeclaredMethod("queueProcessor");
            method.setAccessible(true);
            // main test
            method.invoke(queueProcessorService);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof InterruptedException) {
                LOG.info("Awaited behavior: InterruptedException at QueueProcessorServiceTest.testQueueProcessor(): " + e);
            } else {
                LOG.error("InvocationTargetException at QueueProcessorServiceTest.testQueueProcessor(). Cause: " + e.getCause());
                fail();
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            LOG.error("NoSuchMethodException |  IllegalAccessException at QueueProcessorServiceTest.testQueueProcessor(): " + e);
            fail();
        }

        try {
            // called twice, first iteration, and second
            verify(queueService, times(2)).getQueueFirstElement();
        } catch (InterruptedException e) {
            LOG.error("InterruptedException at QueueProcessorServiceTest.testQueueProcessor(): " + e);
            fail();
        }
        // called once, only at the end of first iteration
        verify(queueService, times(1)).processWebHook(webhookConfigTest);
    }
}
