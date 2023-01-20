/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.config;

import com.lpvs.entity.enums.PullRequestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;


public class WebhookConfigTest {

    Date date = new Date();
    WebhookConfig webhookConfig;

    @BeforeEach
    void setUp() {
        webhookConfig = new WebhookConfig();
        webhookConfig.setId(1L);
        webhookConfig.setAction(PullRequestAction.OPEN);
        webhookConfig.setRepositoryLicense("MIT");
        webhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        webhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        webhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        webhookConfig.setPullRequestAPIUrl("https://github.com/api");
        webhookConfig.setUserId("BestUser");
        webhookConfig.setAttempts(10);
        webhookConfig.setDate(date);
        webhookConfig.setReviewSystemType("scanner");
    }

    @Test
    public void testWebhookConfigEqual() {
        WebhookConfig secondWebhookConfig = new WebhookConfig();
        secondWebhookConfig.setId(1L);
        secondWebhookConfig.setAction(PullRequestAction.OPEN);
        secondWebhookConfig.setRepositoryLicense("MIT");
        secondWebhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        secondWebhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        secondWebhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        secondWebhookConfig.setPullRequestAPIUrl("https://github.com/api");
        secondWebhookConfig.setUserId("BestUser");
        secondWebhookConfig.setAttempts(10);
        secondWebhookConfig.setDate(date);
        secondWebhookConfig.setReviewSystemType("scanner");
        assertEquals(webhookConfig, secondWebhookConfig);
    }

    @Test
    public void testWebhookConfigNotEqual() {
        WebhookConfig secondWebhookConfig = new WebhookConfig();
        secondWebhookConfig.setId(1L);
        secondWebhookConfig.setAction(PullRequestAction.OPEN);
        secondWebhookConfig.setRepositoryLicense("MIT");
        secondWebhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        secondWebhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/17");
        secondWebhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        secondWebhookConfig.setPullRequestAPIUrl("https://github.com/api");
        secondWebhookConfig.setUserId("BestUser");
        secondWebhookConfig.setAttempts(10);
        secondWebhookConfig.setDate(date);
        secondWebhookConfig.setReviewSystemType("scanner");
        assertNotEquals(webhookConfig, secondWebhookConfig);
    }

    @Test
    public void testWebhookConfigToString() {
        assertNotNull(webhookConfig.toString());
    }

    @Test
    public void testWebhookConfigGetters() {
        assertEquals(webhookConfig.getId(), 1L);
        assertEquals(webhookConfig.getAction(), PullRequestAction.OPEN);
        assertEquals(webhookConfig.getRepositoryLicense(), "MIT");
        assertEquals(webhookConfig.getHeadCommitSHA(), "2405d91eebb40e8841465908a0cd9200bba2da12");
        assertEquals(webhookConfig.getPullRequestUrl(), "https://github.com/Samsung/LPVS/pull/16");
        assertEquals(webhookConfig.getPullRequestFilesUrl(), "https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        assertEquals(webhookConfig.getPullRequestAPIUrl(), "https://github.com/api");
        assertEquals(webhookConfig.getUserId(), "BestUser");
        assertEquals(webhookConfig.getAttempts(), 10);
        assertEquals(webhookConfig.getDate(), date);
        assertEquals(webhookConfig.getReviewSystemType(), "scanner");
    }
}
