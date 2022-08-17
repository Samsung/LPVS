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

public class WebhookUtil {

    private static Logger LOG = LoggerFactory.getLogger(WebhookUtil.class);

    public static WebhookConfig getGitHubWebhookConfig(String payload) {
        Gson gson = new Gson();
        WebhookConfig webhookConfig = new WebhookConfig();

        JsonObject json = gson.fromJson(payload, JsonObject.class);
        webhookConfig.setAction(PullRequestAction.convertFrom(json.get("action").getAsString()));
        webhookConfig.setRepositoryName(json.getAsJsonObject("repository")
                                            .get("name").getAsString());
        webhookConfig.setRepositoryOrganization(json.getAsJsonObject("repository")
                                                    .get("full_name").getAsString()
                                                    .split("/")[0]);
        String url = json.getAsJsonObject("pull_request").get("html_url").getAsString();
        webhookConfig.setPullRequestUrl(url);
        if (json.getAsJsonObject("pull_request").getAsJsonObject("head").getAsJsonObject("repo").get("fork").getAsBoolean()) {
            webhookConfig.setPullRequestFilesUrl(json.getAsJsonObject("pull_request").getAsJsonObject("head").getAsJsonObject("repo").get("html_url").getAsString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(json.getAsJsonObject("pull_request").get("url").getAsString());
        webhookConfig.setRepositoryUrl(json.getAsJsonObject("repository").get("html_url").getAsString());
        webhookConfig.setUserId("bot");
        webhookConfig.setPullRequestId(Long.parseLong(url.split("/")[url.split("/").length -1]));
        webhookConfig.setHeadCommitSHA(json.getAsJsonObject("pull_request")
                                            .getAsJsonObject("head")
                                            .get("sha").getAsString());
        webhookConfig.setPullRequestBranch(json.getAsJsonObject("pull_request")
                .getAsJsonObject("head")
                .get("ref").getAsString());
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

}
