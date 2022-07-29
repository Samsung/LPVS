/**
 * Copyright (c) 2022, Samsung Research. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.controller;

import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.service.GitHubService;
import com.lpvs.service.QueueService;
import com.lpvs.util.WebhookUtil;
import com.lpvs.entity.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Date;


@RestController
public class GitHubWebhooksController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private GitHubService gitHubService;

    private static Logger LOG = LoggerFactory.getLogger(GitHubWebhooksController.class);

    private static final String SIGNATURE = "X-Hub-Signature";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";

    @RequestMapping(value = "/webhooks", method = RequestMethod.POST)
    public ResponseEntity<ResponseWrapper> gitHubWebhooks(@RequestHeader(SIGNATURE) String signature, @RequestBody String payload) throws InterruptedException {
        LOG.info("New webhook request received");

        // if signature is empty return 401
        if (!StringUtils.hasText(signature)) {
            return new ResponseEntity<>(new ResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        }

        // if payload is empty, don't do anything
        if (!StringUtils.hasText(payload)) {
            LOG.info("Response to empty payload sent");
            return new ResponseEntity<>(new ResponseWrapper(SUCCESS), HttpStatus.OK);
        } else if (WebhookUtil.checkPayload(payload)) {
            WebhookConfig webhookConfig = WebhookUtil.getGitHubWebhookConfig(payload);
            webhookConfig.setDate(new Date());
            LOG.info("Repository scanning is enabled: On");
            gitHubService.setPendingCheck(webhookConfig);
            queueService.addFirst(webhookConfig);
        }
        LOG.info("Response sent");
        return new ResponseEntity<>(new ResponseWrapper(SUCCESS), HttpStatus.OK);
    }
}
