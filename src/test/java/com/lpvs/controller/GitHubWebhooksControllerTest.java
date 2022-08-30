/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import java.util.*;

//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lpvs.entity.ResponseWrapper;
import com.lpvs.entity.enums.PullRequestAction;
import com.lpvs.controller.GitHubWebhooksController;
import com.lpvs.service.GitHubService;
import com.lpvs.service.QueueService;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;


//package com.lpvs.controller;
//
//import com.lpvs.entity.config.WebhookConfig;
//import com.lpvs.service.GitHubService;
//import com.lpvs.service.QueueService;
//import com.lpvs.util.WebhookUtil;
//import com.lpvs.entity.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestBody;
//import java.util.Date;
//



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

public class GitHubWebhooksControllerTest {

//    @Autowired
//    private QueueService queueService;

//    @Autowired
//    private GitHubService gitHubService;

    private static Logger LOG = LoggerFactory.getLogger(GitHubWebhooksController.class);
    //
    private static final String SIGNATURE = "X-Hub-Signature";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

    QueueService mocked_instance_queueServ = mock(QueueService.class);
    GitHubService mocked_instance_ghServ = mock(GitHubService.class);
    GitHubWebhooksController gitHubWebhooksController = new GitHubWebhooksController(mocked_instance_queueServ, mocked_instance_ghServ);

    @Test
    public void noSignatureTest() {
        ResponseEntity actual;
        try {
            actual = gitHubWebhooksController.gitHubWebhooks(null, null);
        } catch( InterruptedException e) {
            actual = null;
        }
        ResponseEntity expected = new ResponseEntity<>(new ResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        assertEquals(expected.toString().substring(0, 56), actual.toString().substring(0, 56));
    }

    @Test
    public void noPayloadTest() {
        ResponseEntity actual;
        try {
            actual = gitHubWebhooksController.gitHubWebhooks("signature", null);
        } catch( InterruptedException e) {
            actual = null;
        }
        ResponseEntity expected = new ResponseEntity<>(new ResponseWrapper(SUCCESS), HttpStatus.OK);
        assertEquals(expected.toString().substring(0, 42), actual.toString().substring(0, 42));
    }

    @Test
    public void okTest() {
        ResponseEntity actual;

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
            actual = gitHubWebhooksController.gitHubWebhooks("signature", json_to_test);
        } catch( InterruptedException e) {
            actual = null;
        }
        ResponseEntity expected = new ResponseEntity<>(new ResponseWrapper(SUCCESS), HttpStatus.OK);
        assertEquals(expected.toString().substring(0, 42), actual.toString().substring(0, 42));
    }
}
