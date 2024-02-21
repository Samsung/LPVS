/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LicensePreValidationSystemTest {
    final int testNumCores = 42;
    LicensePreValidationSystem licensePreValidationSystem;

    @BeforeEach
    void setUp() {
        licensePreValidationSystem = new LicensePreValidationSystem(42);
    }

    @Test
    public void testGetAsyncExecutor() {
        try (MockedConstruction<ThreadPoolTaskExecutor> mocked =
                mockConstruction(ThreadPoolTaskExecutor.class)) {
            TaskExecutor executor = licensePreValidationSystem.getAsyncExecutor();

            assertEquals(1, mocked.constructed().size());
            ThreadPoolTaskExecutor mocked_constructed_executor = mocked.constructed().get(0);

            // main assert
            assertEquals(executor, mocked_constructed_executor);

            verify(mocked_constructed_executor, times(1)).setCorePoolSize(testNumCores);
            verify(mocked_constructed_executor, times(1)).setThreadNamePrefix("LPVS::");
            verifyNoMoreInteractions(mocked_constructed_executor);
        }
    }
}
