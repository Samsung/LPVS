/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class LPVSWebhookUtil {

    public static LPVSQueue getGitHubWebhookConfig(String payload) {
        Gson gson = new Gson();
        LPVSQueue webhookConfig = new LPVSQueue();

        JsonObject json = gson.fromJson(payload, JsonObject.class);
        webhookConfig.setAction(LPVSPullRequestAction.convertFrom(json.get("action").getAsString()));
        String url = json.getAsJsonObject("pull_request").get("html_url").getAsString();
        webhookConfig.setPullRequestUrl(url);
        if (json.getAsJsonObject("pull_request").getAsJsonObject("head").getAsJsonObject("repo").get("fork").getAsBoolean()) {
            webhookConfig.setPullRequestFilesUrl(json.getAsJsonObject("pull_request").getAsJsonObject("head").getAsJsonObject("repo").get("html_url").getAsString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(json.getAsJsonObject("pull_request").get("url").getAsString());
        webhookConfig.setRepositoryUrl(json.getAsJsonObject("repository").get("html_url").getAsString());
        webhookConfig.setUserId("GitHub hook");
        webhookConfig.setHeadCommitSHA(json.getAsJsonObject("pull_request")
                .getAsJsonObject("head")
                .get("sha").getAsString());
        webhookConfig.setAttempts(0);
        return webhookConfig;
    }

    public static boolean checkPayload(String payload) {
        if (payload.contains("\"zen\":")){
            log.debug("Initial webhook received");
            return false;
        }

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(payload, JsonObject.class);
        String actionString = json.get("action").getAsString();
        log.debug("Action " + actionString);
        LPVSPullRequestAction action = LPVSPullRequestAction.convertFrom(actionString);
        //ToDo: handle all action types
        return (action != null) && (action.equals(LPVSPullRequestAction.UPDATE) || action.equals(LPVSPullRequestAction.OPEN)
                || action.equals(LPVSPullRequestAction.REOPEN));
    }

    public static String getRepositoryOrganization(LPVSQueue webhookConfig) {
        if (null == webhookConfig) {
            log.error("Webhook Config is absent");
            return "Webhook is absent";
        }

        if (null == webhookConfig.getRepositoryUrl()) {
            log.error("No repository url info in webhook config");
            return "No repository url info in webhook config";
        }

        List<String> url = Arrays.asList(webhookConfig.getRepositoryUrl().split("/"));
        return url.get(url.size() - 2);
    }

    public static String getRepositoryName(LPVSQueue webhookConfig) {
        if (null == webhookConfig) {
            log.error("Webhook Config is absent");
            return "Webhook is absent";
        }

        if (null == webhookConfig.getRepositoryUrl()) {
            log.error("No repository url info in webhook config");
            return "No repository url info in webhook config";
        }

        List<String> url = Arrays.asList(webhookConfig.getRepositoryUrl().split("/"));
        return url.get(url.size() - 1);
    }

    public static String getRepositoryUrl(LPVSQueue webhookConfig) {
        return webhookConfig.getRepositoryUrl();
    }

    public static String getPullRequestId(LPVSQueue webhookConfig) {
        if (null == webhookConfig) {
            log.error("Webhook Config is absent");
            return "Webhook is absent";
        }

        if (null == webhookConfig.getRepositoryUrl()) {
            log.error("No repository url info in webhook config");
            return "No repository url info in webhook config";
        }

        List<String> url = Arrays.asList(webhookConfig.getPullRequestUrl().split("/"));
        return url.get(url.size() - 1);
    }
}