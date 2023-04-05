/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.service.scanner.scanoss.LPVSScanossDetectService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@Slf4j
public class LPVSDetectServiceTest {

    @Nested
    class TestInit {
        final LPVSDetectService detectService = new LPVSDetectService("scanoss", null);

        @Test
        public void testInit() {
            try {
                Method init_method = detectService.getClass().getDeclaredMethod("init");
                init_method.setAccessible(true);
                init_method.invoke(detectService);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("LPVSDetectServiceTest::TestInit exception: " + e);
                fail();
            }
        }
    }

    @Nested
    class TestRunScan__Scanoss {
        LPVSDetectService detectService;
        LPVSScanossDetectService scanoss_mock = mock(LPVSScanossDetectService.class);
        LPVSQueue webhookConfig;
        final String test_path = "test_path";

        LPVSFile lpvs_file_1, lpvs_file_2;

        @BeforeEach
        void setUp() {
            detectService = new LPVSDetectService("scanoss", scanoss_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);

            lpvs_file_1 = new LPVSFile(1L, null, null, null, null, null, null, null, null, null, null, null, null);
            lpvs_file_2 = new LPVSFile(2L, null, null, null, null, null, null, null, null, null, null, null, null);

            when(scanoss_mock.checkLicenses(webhookConfig)).thenReturn(List.of(lpvs_file_1, lpvs_file_2));
        }

        @Test
        public void testRunScan__Scanoss() {
            try {
                // main test
                assertEquals(List.of(lpvs_file_1, lpvs_file_2), detectService.runScan(webhookConfig, test_path));
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__Scanoss exception: " + e);
                fail();
            }

            // verify `scanoss_mock`
            try {
                verify(scanoss_mock, times(1)).runScan(webhookConfig, test_path);
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__Scanoss exception: " + e);
                fail();
            }
            verify(scanoss_mock, times(1)).checkLicenses(webhookConfig);
            verifyNoMoreInteractions(scanoss_mock);
        }
    }

    @Nested
    class TestRunScan__ScanossException {
        LPVSDetectService detectService;
        LPVSScanossDetectService scanoss_mock = mock(LPVSScanossDetectService.class);
        LPVSQueue webhookConfig;
        final String test_path = "test_path";
        final String exc_msg = "Test exception for TestRunScan__ScanossException. Normal behavior.";

        @BeforeEach
        void setUp() {
            detectService = new LPVSDetectService("scanoss", scanoss_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);

            try {
                doThrow(new Exception(exc_msg))
                        .when(scanoss_mock)
                        .runScan(webhookConfig, test_path);
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__ScanossException exception: " + e);
                fail();
            }
        }

        @Test
        public void testRunScan__ScanossException() {
            try {
                // main test
                detectService.runScan(webhookConfig, test_path);
                fail("Should exit by exception");
            } catch (Exception e) {
                if (!e.getMessage().equals(exc_msg)) {
                    fail("Another exception caught: " + e);
                }
                // test pass
            }

            // verify `scanoss_mock`
            try {
                verify(scanoss_mock, times(1)).runScan(webhookConfig, test_path);
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__ScanossException exception: " + e);
                fail();
            }
            verifyNoMoreInteractions(scanoss_mock);
        }
    }

    @Nested
    class TestRunScan__NotScanoss {
        LPVSDetectService detectService;

        @BeforeEach
        void setUp() {
            detectService = new LPVSDetectService("not_scanoss", null);
        }

        @Test
        public void testRunScan__NotScanoss() {
            // main test
            try {
                assertEquals(new ArrayList<>(), detectService.runScan(null, null));
            } catch (Exception e) {
                log.error("LPVSDetectServiceTest::TestRunScan__NotScanoss exception: " + e);
                fail();
            }
        }
    }
}
