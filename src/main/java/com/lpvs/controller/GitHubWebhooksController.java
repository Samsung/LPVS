/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.controller;

import com.lpvs.entity.LPVSQueue;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSQueueService;
import com.lpvs.util.LPVSWebhookUtil;
import com.lpvs.entity.LPVSResponseWrapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Date;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@RestController
@Slf4j
public class GitHubWebhooksController {

    private String GITHUB_SECRET;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void setProps() {
        this.GITHUB_SECRET = Optional.ofNullable(this.GITHUB_SECRET).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv("LPVS_GITHUB_SECRET")).orElse(""));
        if (this.GITHUB_SECRET.isEmpty()) {
            log.error("LPVS_GITHUB_SECRET(github.secret) is not set.");
            System.exit(SpringApplication.exit(applicationContext, () -> -1));
        }
    }

    @Autowired
    private LPVSQueueService queueService;

    @Autowired
    private LPVSQueueRepository queueRepository;

    private LPVSGitHubService gitHubService;

    private static final String SIGNATURE = "X-Hub-Signature-256";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";
    private static final String ALGORITHM = "HmacSHA256";

    public GitHubWebhooksController(LPVSQueueService queueService, LPVSGitHubService gitHubService, LPVSQueueRepository queueRepository, @Value("${github.secret:}") String GITHUB_SECRET) {
        this.queueService = queueService;
        this.gitHubService = gitHubService;
        this.queueRepository = queueRepository;
        this.GITHUB_SECRET = GITHUB_SECRET;
    }

    @RequestMapping(value = "/webhooks", method = RequestMethod.POST)
    public ResponseEntity<LPVSResponseWrapper> gitHubWebhooks(@RequestHeader(SIGNATURE) String signature, @RequestBody String payload) throws Exception {
        log.debug("New webhook request received");

        // if signature is empty return 401
        if (!StringUtils.hasText(signature)) {
            return new ResponseEntity<>(new LPVSResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        } else if (!GITHUB_SECRET.trim().isEmpty() && wrongSecret(signature, payload)) {
            log.info("SECRET: " + GITHUB_SECRET);
            log.info("WRONG: " + wrongSecret(signature, payload));
            return new ResponseEntity<>(new LPVSResponseWrapper(ERROR), HttpStatus.FORBIDDEN);
        }

        // if payload is empty, don't do anything
        if (!StringUtils.hasText(payload)) {
            log.debug("Response to empty payload sent");
            return new ResponseEntity<>(new LPVSResponseWrapper(SUCCESS), HttpStatus.OK);
        } else if (LPVSWebhookUtil.checkPayload(payload)) {
            LPVSQueue webhookConfig = LPVSWebhookUtil.getGitHubWebhookConfig(payload);
            webhookConfig.setDate(new Date());
            webhookConfig.setReviewSystemType("github");
            queueRepository.save(webhookConfig);
            log.debug("Pull request scanning is enabled");
            gitHubService.setPendingCheck(webhookConfig);
            log.debug("Set status to Pending done");
            queueService.addFirst(webhookConfig);
            log.debug("Put Webhook config to the queue done");
        }
        log.debug("Response sent");
        return new ResponseEntity<>(new LPVSResponseWrapper(SUCCESS), HttpStatus.OK);
    }

    public boolean wrongSecret(String signature, String payload) throws Exception {
        String lpvsSecret = signature.split("=",2)[1];

        SecretKeySpec key = new SecretKeySpec(GITHUB_SECRET.getBytes("utf-8"), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(key);
        String githubSecret = Hex.encodeHexString(mac.doFinal(payload.getBytes("utf-8")));

        log.debug("lpvs   signature: " + lpvsSecret);
        log.debug("github signature: " + githubSecret);

        return !lpvsSecret.equals(githubSecret);
    }
}
