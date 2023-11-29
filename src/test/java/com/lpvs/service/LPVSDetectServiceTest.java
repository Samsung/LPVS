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
import com.lpvs.util.LPVSFileUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.core.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class LPVSDetectServiceTest {

    @Mock private LPVSGitHubConnectionService gitHubConnectionService;

    @Mock private GitHub gitHub;

    @Mock private GHRepository ghRepository;

    @Mock private GHPullRequest ghPullRequest;

    @Mock private LPVSScanossDetectService scanossDetectService;

    @Mock private ApplicationContext applicationContext;

    @Mock private ApplicationReadyEvent applicationReadyEvent;

    @InjectMocks private LPVSDetectService lpvsDetectService;

    @Nested
    class TestInit {
        final LPVSDetectService detectService = new LPVSDetectService("scanoss", null, null);

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
        LPVSGitHubConnectionService github_mock = mock(LPVSGitHubConnectionService.class);
        LPVSScanossDetectService scanoss_mock = mock(LPVSScanossDetectService.class);
        LPVSQueue webhookConfig;
        final String test_path = "test_path";

        LPVSFile lpvs_file_1, lpvs_file_2;

        @BeforeEach
        void setUp() {
            detectService = new LPVSDetectService("scanoss", github_mock, scanoss_mock);

            webhookConfig = new LPVSQueue();
            webhookConfig.setId(1L);

            lpvs_file_1 =
                    new LPVSFile(
                            1L, null, null, null, null, null, null, null, null, null, null, null,
                            null);
            lpvs_file_2 =
                    new LPVSFile(
                            2L, null, null, null, null, null, null, null, null, null, null, null,
                            null);

            when(scanoss_mock.checkLicenses(webhookConfig))
                    .thenReturn(List.of(lpvs_file_1, lpvs_file_2));
        }

        @Test
        void testRunOneScan_Default() {

            lpvsDetectService = spy(new LPVSDetectService("scanoss", null, scanossDetectService));

            lpvsDetectService.runOneScan();

            assertDoesNotThrow(() -> lpvsDetectService.runOneScan());
        }

        @Test
        void testGetInternalQueueByPullRequest() throws IOException {
            String pullRequest = "github/owner/repo/branch/123";
            when(gitHubConnectionService.connectToGitHubApi()).thenReturn(gitHub);
            when(gitHub.getRepository("owner/repo")).thenReturn(ghRepository);
            when(ghRepository.getPullRequest(123)).thenReturn(ghPullRequest);

            LPVSQueue result = lpvsDetectService.getInternalQueueByPullRequest(pullRequest);

            assertNotNull(result);
            assertEquals(result.getUserId(), "Single scan run");
        }

        @Test
        public void testGetPathByPullRequest() {

            LPVSQueue mockWebhookConfig = mock(LPVSQueue.class);

            String result = LPVSDetectService.getPathByPullRequest(mockWebhookConfig);

            assertNotNull(result);
        }

        @Test
        public void testRunScan__Scanoss() {
            try {
                // main test
                assertEquals(
                        List.of(lpvs_file_1, lpvs_file_2),
                        detectService.runScan(webhookConfig, test_path));
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
        LPVSGitHubConnectionService github_mock = mock(LPVSGitHubConnectionService.class);
        LPVSScanossDetectService scanoss_mock = mock(LPVSScanossDetectService.class);
        LPVSQueue webhookConfig;
        final String test_path = "test_path";
        final String exc_msg = "Test exception for TestRunScan__ScanossException. Normal behavior.";

        @BeforeEach
        void setUp() {
            detectService = new LPVSDetectService("scanoss", github_mock, scanoss_mock);

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
            detectService = new LPVSDetectService("not_scanoss", null, null);
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
