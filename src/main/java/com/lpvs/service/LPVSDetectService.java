/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.google.gson.Gson;
import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.service.scanner.scanoss.LPVSScanossDetectService;
import com.lpvs.util.LPVSExitHandler;
import com.lpvs.util.LPVSFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LPVSDetectService {
    @Value("${github.token}")
    private String GITHUB_AUTH_TOKEN;
    @Value("${github.api.url}")
    private String GITHUB_API_URL;
    @Value("${github.login}")
    private String GITHUB_LOGIN;


    private String scannerType;

    private LPVSScanossDetectService scanossDetectService;

    private final static String GITHUB_LOGIN_PROP_NAME = "github.login";
    private final static String GITHUB_AUTH_TOKEN_PROP_NAME = "github.token";
    private final static String GITHUB_API_URL_PROP_NAME = "github.api.url";

    private final static String GITHUB_LOGIN_ENV_VAR_NAME = "LPVS_GITHUB_LOGIN";
    private final static String GITHUB_AUTH_TOKEN_ENV_VAR_NAME = "LPVS_GITHUB_TOKEN";
    private final static String GITHUB_API_URL_ENV_VAR_NAME = "LPVS_GITHUB_API_URL";

    @Value("${github.pull.request}")
    private String trigger;

    @Autowired
    ApplicationContext ctx;

    @Autowired
    public LPVSDetectService(
            @Value("${scanner:scanoss}") String scannerType,
            LPVSScanossDetectService scanossDetectService) {
        this.scannerType = scannerType;
        this.scanossDetectService = scanossDetectService;
    }

    @PostConstruct
    private void init() {
        this.GITHUB_LOGIN = Optional.ofNullable(GITHUB_LOGIN).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_LOGIN_ENV_VAR_NAME)).orElse(""));
        this.GITHUB_AUTH_TOKEN = Optional.ofNullable(GITHUB_AUTH_TOKEN).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_AUTH_TOKEN_ENV_VAR_NAME)).orElse(""));
        this.GITHUB_API_URL = Optional.ofNullable(GITHUB_API_URL).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_API_URL_ENV_VAR_NAME)).orElse(""));
        log.info("License detection scanner: " + scannerType);
        if (trigger != null && !trigger.equals("")) {
            try {
                LPVSQueue webhookConfig = this.getInternalQueueByPullRequest(trigger);
                this.runScan(webhookConfig, this.getPathByPullRequest(webhookConfig));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @EventListener(ApplicationReadyEvent.class)
    public void exitTheApp() {
        if (trigger != null && !trigger.equals("")) {
            doThisStuffToExit();
        }
    }

    @Async("threadPoolTaskExecutor")
    public void doThisStuffToExit() {
        System.out.println("Exiting...");
        System.exit(0);
    }

    private static GitHub gitHub;

    public void setGithubTokenFromEnv() {
        if (System.getenv("LPVS_GITHUB_TOKEN") != null) GITHUB_AUTH_TOKEN = System.getenv("LPVS_GITHUB_TOKEN");
    }

    private static LPVSQueue getGitHubWebhookConfig(GHRepository repo, GHPullRequest pR) {
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setPullRequestUrl(pR.getHtmlUrl() != null ? pR.getHtmlUrl().toString() : null);
        if (pR.getHead() != null && pR.getHead().getRepository() != null
                && pR.getHead().getRepository().getHtmlUrl() != null) {
            webhookConfig.setPullRequestFilesUrl(pR.getHead().getRepository().getHtmlUrl().toString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(pR.getUrl() != null ? pR.getUrl().toString() : null);
        webhookConfig.setRepositoryUrl(repo.getHtmlUrl() != null ? repo.getHtmlUrl().toString() : null);
        webhookConfig.setUserId("Single scan run");
        webhookConfig.setHeadCommitSHA(pR.getHead() != null ? pR.getHead().getSha() : null);
        return webhookConfig;
    }

    private LPVSQueue getInternalQueueByPullRequest(String pullRequest) {
        try {
            if (pullRequest == null) return null;
            String[] pullRequestSplit = pullRequest.split("/");
            if (pullRequestSplit.length != 3) return null;
            String pullRequestRepo = String.join("/", Arrays
                    .asList(pullRequestSplit)
                    .subList(0, pullRequestSplit.length - 1));
            int pullRequestNum = Integer.parseInt(pullRequestSplit[pullRequestSplit.length - 1]);
            if (GITHUB_AUTH_TOKEN == null
                    || GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL == null
                    || GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repo = gitHub.getRepository(pullRequestRepo);
            GHPullRequest pR = repo.getPullRequest(pullRequestNum);
            return LPVSDetectService.getGitHubWebhookConfig(repo, pR);
        } catch (IOException e){
            log.error("Can't set up github client: " + e);
        }
        return null;
    }

    private String getPathByPullRequest(LPVSQueue webhookConfig) {
        if (webhookConfig == null) return null;
        return LPVSFileUtil.getLocalDirectoryPath(webhookConfig);
    }

    public List<LPVSFile> runScan(LPVSQueue webhookConfig, String path) throws Exception {
        if (scannerType.equals("scanoss")) {
            scanossDetectService.runScan(webhookConfig, path);
            return scanossDetectService.checkLicenses(webhookConfig);
        }
        return new ArrayList<>();
    }
}
