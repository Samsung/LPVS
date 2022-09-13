/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
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

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
public class GitHubWebhooksController {
    @Value("${github.secret:}")
    private String GITHUB_SECRET;

    private QueueService queueService;

    private GitHubService gitHubService;

    private static Logger LOG = LoggerFactory.getLogger(GitHubWebhooksController.class);

    private static final String SIGNATURE = "X-Hub-Signature-256";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";
    private static final String ALGORITHM = "HmacSHA256";

    public GitHubWebhooksController(QueueService queueService, GitHubService gitHubService, @Value("${github.secret:}") String GITHUB_SECRET) {
        this.queueService = queueService;
        this.gitHubService = gitHubService;
        this.GITHUB_SECRET = GITHUB_SECRET;
    }

    @RequestMapping(value = "/webhooks", method = RequestMethod.POST)
    public ResponseEntity<ResponseWrapper> gitHubWebhooks(@RequestHeader(SIGNATURE) String signature, @RequestBody String payload) throws Exception {
        LOG.info("New webhook request received");

        // if signature is empty return 401
        if (!StringUtils.hasText(signature)) {
            return new ResponseEntity<>(new ResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        } else if (!GITHUB_SECRET.trim().isEmpty() && wrongSecret(signature, payload)) {
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

    public boolean wrongSecret(String signature, String payload) throws Exception {
        String lpvsSecret = signature.split("=",2)[1];

        SecretKeySpec key = new SecretKeySpec(GITHUB_SECRET.getBytes("utf-8"), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(key);
        String githubSecret = Hex.encodeHexString(mac.doFinal(payload.getBytes()));

        LOG.info("lpvs   signature: " + lpvsSecret);
        LOG.info("github signature: " + githubSecret);

        return !lpvsSecret.equals(githubSecret);
    }
}
