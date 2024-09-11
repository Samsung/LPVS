/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs;

import com.lpvs.util.LPVSExitHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
public class LicensePreValidationServiceTest {

    LicensePreValidationService licensePreValidationService;

    @Mock SpringApplication springApplication;

    @Mock ConfigurableApplicationContext applicationContext;

    @Mock LPVSExitHandler exitHandler;

    String[] args = new String[0];

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        licensePreValidationService =
                new LicensePreValidationService() {
                    @Override
                    protected SpringApplication createSpringApplication() {
                        return springApplication;
                    }
                };
        doNothing().when(springApplication).addInitializers(any());
        when(springApplication.run()).thenReturn(applicationContext);
    }

    @Test
    public void testMain() {
        Mockito.when(applicationContext.getBean(LPVSExitHandler.class)).thenReturn(exitHandler);
        licensePreValidationService.run(args);
        Mockito.verify(applicationContext).getBean(LPVSExitHandler.class);
    }

    @Test
    public void testMain_IllegalAccessException_N() {
        when(applicationContext.getBean(LPVSExitHandler.class))
                .thenReturn(exitHandler)
                .thenThrow(new IllegalArgumentException("Test IllegalArgumentException"));
        licensePreValidationService.run(args); // First call - initialize exitHandler
        licensePreValidationService.run(args); // Second call - IllegalAccessException
        Mockito.verify(applicationContext, Mockito.times(2)).getBean(LPVSExitHandler.class);
        Mockito.verify(exitHandler, Mockito.times(1)).exit(anyInt());
    }

    @Test
    public void testMain_Exception_N() throws NoSuchFieldException, IllegalAccessException {
        Mockito.doThrow(new RuntimeException("Test RuntimeException"))
                .when(applicationContext)
                .getBean(LPVSExitHandler.class);
        licensePreValidationService.run(args);
        Mockito.verify(exitHandler, Mockito.times(0)).exit(anyInt());
    }

    @Test
    public void testAddInitializers() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Mockito.when(applicationContext.getBean(LPVSExitHandler.class)).thenReturn(exitHandler);
        ArgumentCaptor<ApplicationContextInitializer<ConfigurableApplicationContext>> captor =
                ArgumentCaptor.forClass(ApplicationContextInitializer.class);
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("lpvs.cores", "8")).thenReturn("8");
        when(env.getProperty("lpvs.version", "Unknown")).thenReturn("1.0.0");

        licensePreValidationService.run(args);

        verify(springApplication).addInitializers(captor.capture());

        ApplicationContextInitializer<ConfigurableApplicationContext> initializer =
                captor.getValue();
        initializer.initialize(applicationContext);

        assertTrue(outContent.toString().contains("1.0.0"));
        System.setOut(originalOut);
    }

    @Test
    public void testCreateSpringApplication() {
        LicensePreValidationService service = new LicensePreValidationService();
        SpringApplication springApplication = service.createSpringApplication();
        assertNotNull(springApplication);
    }

    @Test
    public void testGetAsyncExecutor() {
        try (MockedConstruction<ThreadPoolTaskExecutor> mocked =
                mockConstruction(ThreadPoolTaskExecutor.class)) {
            LicensePreValidationService lpvs = new LicensePreValidationService();
            TaskExecutor executor = lpvs.getAsyncExecutor();

            assertEquals(1, mocked.constructed().size());
            ThreadPoolTaskExecutor mocked_constructed_executor = mocked.constructed().get(0);

            assertEquals(executor, mocked_constructed_executor);
            verify(mocked_constructed_executor, times(1)).setCorePoolSize(8);
            verify(mocked_constructed_executor, times(1)).setThreadNamePrefix("LPVS::");
            verifyNoMoreInteractions(mocked_constructed_executor);
        }
    }

    @Test
    public void testGetEmblem() {
        String emblem = LicensePreValidationService.getEmblem("test");
        assertNotNull(emblem);
        assertTrue(emblem.contains("test"));
    }
}
