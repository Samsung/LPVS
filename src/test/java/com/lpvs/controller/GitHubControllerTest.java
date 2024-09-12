/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.LPVSResponseWrapper;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSQueueService;

import com.lpvs.util.LPVSExitHandler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Method;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SystemStubsExtension.class)
public class GitHubControllerTest {

    @SystemStub private EnvironmentVariables environmentVars;

    private static final String SIGNATURE =
            "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

    private LPVSExitHandler exitHandler;

    GitHub gitHub = mock(GitHub.class);
    GHRepository ghRepository = mock(GHRepository.class);
    GHPullRequest ghPullRequest = mock(GHPullRequest.class);
    LPVSQueueService mocked_instance_queueServ = mock(LPVSQueueService.class);
    LPVSGitHubService mocked_instance_ghServ = mock(LPVSGitHubService.class);
    LPVSQueueRepository mocked_queueRepo = mock(LPVSQueueRepository.class);
    LPVSGitHubConnectionService mocked_ghConnServ = mock(LPVSGitHubConnectionService.class);
    GitHubController gitHubController =
            new GitHubController(
                    mocked_instance_queueServ,
                    mocked_instance_ghServ,
                    mocked_ghConnServ,
                    mocked_queueRepo,
                    "",
                    exitHandler);

    GitHubController gitHubControllerWrongSecret =
            new GitHubController(
                    mocked_instance_queueServ,
                    mocked_instance_ghServ,
                    mocked_ghConnServ,
                    mocked_queueRepo,
                    "LPVS",
                    exitHandler);

    @Test
    void ForwardToWebhookTest() throws ServletException, IOException {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        RequestDispatcher mockDispatcher = mock(RequestDispatcher.class);

        when(mockRequest.getRequestDispatcher("/webhooks")).thenReturn(mockDispatcher);

        // Act
        gitHubController.forwardToWebhook(mockRequest, mockResponse);

        // Assert
        verify(mockRequest).getRequestDispatcher("/webhooks");
        verify(mockDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void noSignatureTest() {
        ResponseEntity<LPVSResponseWrapper> actual;
        try {
            actual = gitHubController.gitHubWebhooks(null, null);
        } catch (Exception e) {
            actual = null;
        }
        ResponseEntity<LPVSResponseWrapper> expected =
                new ResponseEntity<>(new LPVSResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        assertEquals(expected.toString().substring(0, 56), actual.toString().substring(0, 56));
    }

    @Test
    public void wrongGithubSecretTest() {
        ResponseEntity<LPVSResponseWrapper> actual;
        try {
            actual = gitHubControllerWrongSecret.gitHubWebhooks(SIGNATURE, "test");
        } catch (Exception e) {
            actual = null;
        }
        ResponseEntity<LPVSResponseWrapper> expected =
                new ResponseEntity<>(new LPVSResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        assertEquals(expected.toString().substring(0, 56), actual.toString().substring(0, 56));
    }

    @Test
    public void noPayloadTest() {
        ResponseEntity<LPVSResponseWrapper> actual;
        try {
            actual = gitHubController.gitHubWebhooks(SIGNATURE, null);
        } catch (Exception e) {
            actual = null;
        }
        ResponseEntity<LPVSResponseWrapper> expected =
                new ResponseEntity<>(new LPVSResponseWrapper(SUCCESS), HttpStatus.OK);
        assertEquals(expected.toString().substring(0, 42), actual.toString().substring(0, 42));
    }

    @Test
    public void okTest() {
        ResponseEntity<LPVSResponseWrapper> actual;

        String json_to_test =
                "{"
                        + "\"action\": \"opened\", "
                        + "\"repository\": {"
                        + "\"name\": \"LPVS\", "
                        + "\"full_name\": \"Samsung/LPVS\", "
                        + "\"html_url\": \"https://github.com/Samsung/LPVS\""
                        + "}, "
                        + "\"pull_request\": {"
                        + "\"html_url\": \"https://github.com/Samsung/LPVS/pull/18\", "
                        + "\"base\": {"
                        + "\"repo\": {"
                        + "\"owner\": {"
                        + "\"login\": \"Samsung\""
                        + "}"
                        + "}"
                        + "},"
                        + "\"head\": {"
                        + "\"repo\": {"
                        + "\"owner\": {"
                        + "\"login\": \"o-kopysov\""
                        + "},"
                        + "\"fork\": true, "
                        + "\"html_url\": \"https://github.com/o-kopysov/LPVS/tree/utests\""
                        + "}, "
                        + "\"sha\": \"edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9\", "
                        + "\"ref\": \"o-kopysov:utests\""
                        + "}, "
                        + "\"url\": \"https://api.github.com/repos/Samsung/LPVS/pulls/18\""
                        + "},"
                        + "\"sender\": {"
                        + "\"login\": \"o-kopysov\""
                        + "}"
                        + "}";

        try {
            actual = gitHubController.gitHubWebhooks(SIGNATURE, json_to_test);
        } catch (Exception e) {
            log.error(e.getMessage());
            actual = null;
            fail();
        }
        ResponseEntity<LPVSResponseWrapper> expected =
                new ResponseEntity<>(new LPVSResponseWrapper(SUCCESS), HttpStatus.OK);
        assertEquals(expected.toString().substring(0, 42), actual.toString().substring(0, 42));
    }

    @Test
    public void wrongSecretTest() {

        environmentVars.set("LPVS_GITHUB_SECRET", "LPVS");

        String signature =
                "sha256=c0ca451d2e2a7ea7d50bb29383996a35f43c7a9df0810bd6ffc45cefc8d1ce42";

        String json_to_test =
                "{"
                        + "\"action\": \"opened\", "
                        + "\"repository\": {"
                        + "\"name\": \"LPVS\", "
                        + "\"full_name\": \"Samsung/LPVS\", "
                        + "\"html_url\": \"https://github.com/Samsung/LPVS\""
                        + "}, "
                        + "\"pull_request\": {"
                        + "\"html_url\": \"https://github.com/Samsung/LPVS/pull/18\", "
                        + "\"head\": {"
                        + "\"repo\": {"
                        + "\"fork\": true, "
                        + "\"html_url\": \"https://github.com/o-kopysov/LPVS/tree/utests\""
                        + "}, "
                        + "\"sha\": \"edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9\", "
                        + "\"ref\": \"o-kopysov:utests\""
                        + "}, "
                        + "\"url\": \"https://api.github.com/repos/Samsung/LPVS/pulls/18\""
                        + "}"
                        + "}";
        try {
            gitHubController.initializeGitHubController();
            boolean secret = gitHubController.wrongSecret(signature, json_to_test);
            assertEquals(secret, false);
            secret = gitHubController.wrongSecret(signature + " ", json_to_test);
            assertEquals(secret, true);
        } catch (Exception e) {
            log.error("GitHubControllerTest::wrongSecretTest exception: " + e);
            fail();
        }
    }

    @Test
    public void noSecretSetTest() {

        environmentVars.set("", "LPVS");

        try {
            gitHubController.initializeGitHubController();
            fail("Expected Exception was not thrown");
        } catch (NullPointerException e) {
            // Test passes if a NullPointerException is caught during access to null pointer
            // If we remove ExitHandler any time, this behaviour should be changed
            log.info(
                    "GitHubControllerTest::noSecretSetTest passed with NullPointerException: " + e);
        } catch (Exception e) {
            // Test fails if any other exception is caught
            log.error(
                    "GitHubControllerTest::noSecretSetTest failed with unexpected exception: " + e);
            fail("Unexpected exception thrown: " + e);
        }
    }

    @Test
    public void testGitHubSingleScan_Success() throws Exception {
        environmentVars.set("LPVS_GITHUB_SECRET", "LPVS");
        Method method = gitHubController.getClass().getDeclaredMethod("initializeGitHubController");
        method.setAccessible(true);
        method.invoke(gitHubController);
        LPVSQueue mockScanConfig = new LPVSQueue();
        when(mocked_instance_ghServ.getInternalQueueByPullRequest(anyString()))
                .thenReturn(mockScanConfig);
        when(mocked_queueRepo.save(any())).thenReturn(mockScanConfig);
        doNothing().when(mocked_instance_queueServ).addFirst(any());

        when(mocked_ghConnServ.connectToGitHubApi()).thenReturn(gitHub);
        when(gitHub.getRepository("org/repo")).thenReturn(ghRepository);
        when(ghRepository.getPullRequest(1)).thenReturn(ghPullRequest);

        ResponseEntity<LPVSResponseWrapper> responseEntity =
                gitHubController.gitHubSingleScan("org", "repo", 1);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGitHubSingleScan_InvalidSecret() throws Exception {
        environmentVars.set("LPVS_GITHUB_SECRET", "LPVS");
        when(mocked_instance_ghServ.getInternalQueueByPullRequest(anyString())).thenReturn(null);
        ResponseEntity<LPVSResponseWrapper> responseEntity =
                gitHubController.gitHubSingleScan("org", "repo", 1);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    public void testGitHubSingleScan_ConnectionError() throws Exception {
        environmentVars.set("LPVS_GITHUB_SECRET", "LPVS");
        when(mocked_instance_ghServ.getInternalQueueByPullRequest(anyString()))
                .thenThrow(new RuntimeException("Connection error"));
        ResponseEntity<LPVSResponseWrapper> responseEntity =
                gitHubControllerWrongSecret.gitHubSingleScan("org", "repo", 1);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }
}
