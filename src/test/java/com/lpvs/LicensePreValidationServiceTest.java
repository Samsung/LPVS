/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs;

import com.lpvs.util.LPVSExitHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LicensePreValidationServiceTest {
    final int testNumCores = 42;
    LicensePreValidationService licensePreValidationService;

    private MockedStatic<SpringApplication> mockedStatic;

    @BeforeEach
    void setUp() {
        licensePreValidationService = new LicensePreValidationService(42);
        mockedStatic = Mockito.mockStatic(SpringApplication.class);
    }

    @AfterEach
    public void tearDown() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    public void testGetAsyncExecutor() {
        try (MockedConstruction<ThreadPoolTaskExecutor> mocked =
                mockConstruction(ThreadPoolTaskExecutor.class)) {
            TaskExecutor executor = licensePreValidationService.getAsyncExecutor();

            assertEquals(1, mocked.constructed().size());
            ThreadPoolTaskExecutor mocked_constructed_executor = mocked.constructed().get(0);

            // main assert
            assertEquals(executor, mocked_constructed_executor);

            verify(mocked_constructed_executor, times(1)).setCorePoolSize(testNumCores);
            verify(mocked_constructed_executor, times(1)).setThreadNamePrefix("LPVS::");
            verifyNoMoreInteractions(mocked_constructed_executor);
        }
    }

    @Test
    public void testMain() {
        ConfigurableApplicationContext applicationContext =
                Mockito.mock(ConfigurableApplicationContext.class);
        LPVSExitHandler exitHandler = Mockito.mock(LPVSExitHandler.class);
        String[] args = new String[0];

        mockedStatic
                .when(() -> SpringApplication.run(LicensePreValidationService.class, args))
                .thenReturn(applicationContext);
        Mockito.when(applicationContext.getBean(LPVSExitHandler.class)).thenReturn(exitHandler);
        LicensePreValidationService.main(args);
        Mockito.verify(applicationContext).getBean(LPVSExitHandler.class);
    }

    @Test
    public void testMain_IllegalAccessException_N()
            throws NoSuchFieldException, IllegalAccessException {
        ConfigurableApplicationContext applicationContext =
                Mockito.mock(ConfigurableApplicationContext.class);
        LPVSExitHandler exitHandler = Mockito.mock(LPVSExitHandler.class);
        String[] args = new String[0];

        mockedStatic
                .when(() -> SpringApplication.run(LicensePreValidationService.class, args))
                .thenReturn(applicationContext);

        Field exitHandlerField = LicensePreValidationService.class.getDeclaredField("exitHandler");
        exitHandlerField.setAccessible(true);
        exitHandlerField.set(null, exitHandler);

        Mockito.doThrow(new IllegalArgumentException("Test IllegalArgumentException"))
                .when(applicationContext)
                .getBean(LPVSExitHandler.class);
        LicensePreValidationService.main(args);
        Mockito.verify(exitHandler, Mockito.times(1)).exit(anyInt());
    }

    @Test
    public void testMain_Exception_N() throws NoSuchFieldException, IllegalAccessException {
        ConfigurableApplicationContext applicationContext =
                Mockito.mock(ConfigurableApplicationContext.class);
        LPVSExitHandler exitHandler = Mockito.mock(LPVSExitHandler.class);
        String[] args = new String[0];

        mockedStatic
                .when(() -> SpringApplication.run(LicensePreValidationService.class, args))
                .thenReturn(applicationContext);

        Field exitHandlerField = LicensePreValidationService.class.getDeclaredField("exitHandler");
        exitHandlerField.setAccessible(true);
        exitHandlerField.set(null, exitHandler);

        Mockito.doThrow(new RuntimeException("Test RuntimeException"))
                .when(applicationContext)
                .getBean(LPVSExitHandler.class);
        LicensePreValidationService.main(args);
        Mockito.verify(exitHandler, Mockito.times(0)).exit(anyInt());
    }
}
