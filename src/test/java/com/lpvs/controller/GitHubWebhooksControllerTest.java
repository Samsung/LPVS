/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.entity.ResponseWrapper;
import com.lpvs.service.GitHubService;
import com.lpvs.service.QueueService;

import org.junit.jupiter.api.Test;

import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class GitHubWebhooksControllerTest {

    private static final String SIGNATURE = "X-Hub-Signature-256";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

    private static Logger LOG = LoggerFactory.getLogger(GitHubWebhooksControllerTest.class);

    QueueService mocked_instance_queueServ = mock(QueueService.class);
    GitHubService mocked_instance_ghServ = mock(GitHubService.class);
    GitHubWebhooksController gitHubWebhooksController = new GitHubWebhooksController(mocked_instance_queueServ, mocked_instance_ghServ, "");

    @Test
    public void noSignatureTest() {
        ResponseEntity<ResponseWrapper> actual;
        try {
            actual = gitHubWebhooksController.gitHubWebhooks(null, null);
        } catch( Exception e) {
            actual = null;
        }
        ResponseEntity<ResponseWrapper> expected = new ResponseEntity<>(new ResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        assertEquals(expected.toString().substring(0, 56), actual.toString().substring(0, 56));
    }

    @Test
    public void noPayloadTest() {
        ResponseEntity<ResponseWrapper> actual;
        try {
            actual = gitHubWebhooksController.gitHubWebhooks(SIGNATURE, null);
        } catch( Exception e) {
            actual = null;
        }
        ResponseEntity<ResponseWrapper> expected = new ResponseEntity<>(new ResponseWrapper(SUCCESS), HttpStatus.OK);
        assertEquals(expected.toString().substring(0, 42), actual.toString().substring(0, 42));
    }

    @Test
    public void okTest() {
        ResponseEntity<ResponseWrapper> actual;

        String  json_to_test =
            "{" +
                "\"action\": \"opened\", " +
                "\"repository\": {" +
                    "\"name\": \"LPVS\", " +
                    "\"full_name\": \"Samsung/LPVS\", " +
                    "\"html_url\": \"https://github.com/Samsung/LPVS\"" +
                "}, " +
                "\"pull_request\": {" +
                    "\"html_url\": \"https://github.com/Samsung/LPVS/pull/18\", " +
                    "\"head\": {" +
                        "\"repo\": {" +
                            "\"fork\": true, " +
                            "\"html_url\": \"https://github.com/o-kopysov/LPVS/tree/utests\"" +
                        "}, " +
                        "\"sha\": \"edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9\", " +
                        "\"ref\": \"o-kopysov:utests\"" +
                    "}, " +
                    "\"url\": \"https://api.github.com/repos/Samsung/LPVS/pulls/18\"" +
                "}" +
            "}";

        try {
            actual = gitHubWebhooksController.gitHubWebhooks(SIGNATURE, json_to_test);
        } catch( Exception e) {
            actual = null;
        }
        ResponseEntity<ResponseWrapper> expected = new ResponseEntity<>(new ResponseWrapper(SUCCESS), HttpStatus.OK);
        assertEquals(expected.toString().substring(0, 42), actual.toString().substring(0, 42));
    }

    @Test
    @SetEnvironmentVariable(key = "LPVS_GITHUB_SECRET", value = "LPVS")
    public void wrongSecretTest() {

        String signature = "sha256=c0ca451d2e2a7ea7d50bb29383996a35f43c7a9df0810bd6ffc45cefc8d1ce42";

        String  json_to_test =
                "{" +
                        "\"action\": \"opened\", " +
                        "\"repository\": {" +
                        "\"name\": \"LPVS\", " +
                        "\"full_name\": \"Samsung/LPVS\", " +
                        "\"html_url\": \"https://github.com/Samsung/LPVS\"" +
                        "}, " +
                        "\"pull_request\": {" +
                        "\"html_url\": \"https://github.com/Samsung/LPVS/pull/18\", " +
                        "\"head\": {" +
                        "\"repo\": {" +
                        "\"fork\": true, " +
                        "\"html_url\": \"https://github.com/o-kopysov/LPVS/tree/utests\"" +
                        "}, " +
                        "\"sha\": \"edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9\", " +
                        "\"ref\": \"o-kopysov:utests\"" +
                        "}, " +
                        "\"url\": \"https://api.github.com/repos/Samsung/LPVS/pulls/18\"" +
                        "}" +
                        "}";
        try {
            gitHubWebhooksController.setProps();
            boolean secret = gitHubWebhooksController.wrongSecret(signature, json_to_test);
            assertEquals(secret, false);
            secret = gitHubWebhooksController.wrongSecret(signature + " ", json_to_test);
            assertEquals(secret, true);
        } catch (Exception e) {
            LOG.error("GitHubWebhooksControllerTest::wrongSecretTest exception: " + e);
            fail();
        }
    }
}
