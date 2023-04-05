/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSQueue;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@Slf4j
public class LPVSQueueProcessorServiceTest {

    LPVSQueueProcessorService queueProcessorService = mock(LPVSQueueProcessorService.class);
    LPVSQueue webhookConfigTest = mock(LPVSQueue.class);
    LPVSQueueService queueService = mock(LPVSQueueService.class);;

    @BeforeEach
    void setUp() {
        webhookConfigTest = new LPVSQueue();
        queueService =mock(LPVSQueueService.class);

        try {
            when(queueService.getQueueFirstElement())
                    // first iteration
                    .thenReturn(webhookConfigTest)
                    // second iteration
                    .thenThrow(new InterruptedException("Test InterruptedException at 2nd iteration"));
        } catch (InterruptedException e) {
            log.error("InterruptedException at LPVSQueueProcessorServiceTest.setUp(): " + e);
            fail();
        }

        queueProcessorService = new LPVSQueueProcessorService(queueService);
    }

    @Test
    public void testQueueProcessor() throws IOException {
        // first iteration of loop inside method passes, second stops by Exception
        try {
            Method method = queueProcessorService.getClass().getDeclaredMethod("queueProcessor");
            method.setAccessible(true);
            // main test
            method.invoke(queueProcessorService);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof InterruptedException) {
                log.info("Awaited behavior: InterruptedException at LPVSQueueProcessorServiceTest.testQueueProcessor(): " + e);
            } else {
                log.error("InvocationTargetException at LPVSQueueProcessorServiceTest.testQueueProcessor(). Cause: " + e.getCause());
                fail();
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            log.error("NoSuchMethodException |  IllegalAccessException at LPVSQueueProcessorServiceTest.testQueueProcessor(): " + e);
            fail();
        }

        try {
            // called twice, first iteration, and second
            verify(queueService, times(2)).getQueueFirstElement();
        } catch (InterruptedException e) {
            log.error("InterruptedException at LPVSQueueProcessorServiceTest.testQueueProcessor(): " + e);
            fail();
        }
        // called once, only at the end of first iteration
        verify(queueService, times(1)).processWebHook(webhookConfigTest);
    }


}
