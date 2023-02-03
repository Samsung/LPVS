/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.entity.enums.PullRequestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class WebhookUtil {

    private static Logger LOG = LoggerFactory.getLogger(WebhookUtil.class);

    public static WebhookConfig getGitHubWebhookConfig(String payload) {
        Gson gson = new Gson();
        WebhookConfig webhookConfig = new WebhookConfig();

        JsonObject json = gson.fromJson(payload, JsonObject.class);
        webhookConfig.setAction(PullRequestAction.convertFrom(json.get("action").getAsString()));
        String url = json.getAsJsonObject("pull_request").get("html_url").getAsString();
        webhookConfig.setPullRequestUrl(url);
        if (json.getAsJsonObject("pull_request").getAsJsonObject("head").getAsJsonObject("repo").get("fork").getAsBoolean()) {
            webhookConfig.setPullRequestFilesUrl(json.getAsJsonObject("pull_request").getAsJsonObject("head").getAsJsonObject("repo").get("html_url").getAsString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(json.getAsJsonObject("pull_request").get("url").getAsString());
        webhookConfig.setUserId("bot");
        webhookConfig.setHeadCommitSHA(json.getAsJsonObject("pull_request")
                                            .getAsJsonObject("head")
                                            .get("sha").getAsString());
        webhookConfig.setAttempts(0);
        return webhookConfig;
    }

    public static boolean checkPayload(String payload) {
        if (payload.contains("\"zen\":")){
            LOG.info("Initial webhook received");
            return false;
        }

        LOG.info("PAYLOAD: " + payload);

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(payload, JsonObject.class);
        String actionString = json.get("action").getAsString();
        LOG.info("Action " + actionString);
        PullRequestAction action = PullRequestAction.convertFrom(actionString);
        //ToDo: handle all action types
        return (action != null) && (action.equals(PullRequestAction.UPDATE) || action.equals(PullRequestAction.OPEN)
                                                                            || action.equals(PullRequestAction.REOPEN));
    }

    public static String getRepositoryOrganization(WebhookConfig webhookConfig) {
        List<String> url = Arrays.asList(webhookConfig.getPullRequestUrl().split("/"));
        int index = url.indexOf("pull");
        return url.get(index - 2);
    }

    public static String getRepositoryName(WebhookConfig webhookConfig) {
        List<String> url = Arrays.asList(webhookConfig.getPullRequestUrl().split("/"));
        int index = url.indexOf("pull");
        return url.get(index - 1);
    }

    public static String getRepositoryUrl(WebhookConfig webhookConfig) {
        int index = webhookConfig.getPullRequestUrl().indexOf("/pull");
        return webhookConfig.getPullRequestUrl().substring(0, index);
    }

    public static String getPullRequestId(WebhookConfig webhookConfig) {
        List<String> url = Arrays.asList(webhookConfig.getPullRequestUrl().split("/"));
        return url.get(url.size() - 1);
    }

}
