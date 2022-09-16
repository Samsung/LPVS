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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GitHubWebhooksControllerTest {

    private static final String SIGNATURE = "X-Hub-Signature-256";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

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
}
