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
        webhookConfig.setWebhookId(1L);
        webhookConfig.setAction(PullRequestAction.OPEN);
        webhookConfig.setRepositoryId(100L);
        webhookConfig.setRepositoryName("Test");
        webhookConfig.setRepositoryOrganization("LPVS");
        webhookConfig.setRepositoryUrl("https://github.com/samsung/lpvs");
        webhookConfig.setRepositoryLicense("MIT");
        webhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        webhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        webhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        webhookConfig.setPullRequestAPIUrl("https://github.com/api");
        webhookConfig.setPullRequestId(10L);
        webhookConfig.setPullRequestName("Test");
        webhookConfig.setUserId("BestUser");
        webhookConfig.setHubLink("https://some.link");
        webhookConfig.setBranch("main");
        webhookConfig.setPullRequestBranch("test");
        webhookConfig.setAttempts(10);
        webhookConfig.setDate(date);
        webhookConfig.setReviewSystemType("scanner");
        webhookConfig.setReviewSystemName("scanoss");
        webhookConfig.setStatusCallbackUrl("https://some.url");
    }

    @Test
    public void testWebhookConfigEqual() {
        WebhookConfig secondWebhookConfig = new WebhookConfig();
        secondWebhookConfig.setWebhookId(1L);
        secondWebhookConfig.setAction(PullRequestAction.OPEN);
        secondWebhookConfig.setRepositoryId(100L);
        secondWebhookConfig.setRepositoryName("Test");
        secondWebhookConfig.setRepositoryOrganization("LPVS");
        secondWebhookConfig.setRepositoryUrl("https://github.com/samsung/lpvs");
        secondWebhookConfig.setRepositoryLicense("MIT");
        secondWebhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        secondWebhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        secondWebhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        secondWebhookConfig.setPullRequestAPIUrl("https://github.com/api");
        secondWebhookConfig.setPullRequestId(10L);
        secondWebhookConfig.setPullRequestName("Test");
        secondWebhookConfig.setUserId("BestUser");
        secondWebhookConfig.setHubLink("https://some.link");
        secondWebhookConfig.setBranch("main");
        secondWebhookConfig.setPullRequestBranch("test");
        secondWebhookConfig.setAttempts(10);
        secondWebhookConfig.setDate(date);
        secondWebhookConfig.setReviewSystemType("scanner");
        secondWebhookConfig.setReviewSystemName("scanoss");
        secondWebhookConfig.setStatusCallbackUrl("https://some.url");
        assertEquals(webhookConfig, secondWebhookConfig);
    }

    @Test
    public void testWebhookConfigNotEqual() {
        WebhookConfig secondWebhookConfig = new WebhookConfig();
        secondWebhookConfig.setWebhookId(1L);
        secondWebhookConfig.setAction(PullRequestAction.OPEN);
        secondWebhookConfig.setRepositoryId(100L);
        secondWebhookConfig.setRepositoryName("Test1");
        secondWebhookConfig.setRepositoryOrganization("LPVS");
        secondWebhookConfig.setRepositoryUrl("https://github.com/samsung/lpvs");
        secondWebhookConfig.setRepositoryLicense("MIT");
        secondWebhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        secondWebhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        secondWebhookConfig.setPullRequestFilesUrl("https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        secondWebhookConfig.setPullRequestAPIUrl("https://github.com/api");
        secondWebhookConfig.setPullRequestId(10L);
        secondWebhookConfig.setPullRequestName("Test");
        secondWebhookConfig.setUserId("BestUser");
        secondWebhookConfig.setHubLink("https://some.link");
        secondWebhookConfig.setBranch("main");
        secondWebhookConfig.setPullRequestBranch("test");
        secondWebhookConfig.setAttempts(10);
        secondWebhookConfig.setDate(date);
        secondWebhookConfig.setReviewSystemType("scanner");
        secondWebhookConfig.setReviewSystemName("scanoss");
        secondWebhookConfig.setStatusCallbackUrl("https://some.url");
        assertNotEquals(webhookConfig, secondWebhookConfig);
    }

    @Test
    public void testWebhookConfigToString() {
        assertNotNull(webhookConfig.toString());
    }

    @Test
    public void testWebhookConfigGetters() {
        assertEquals(webhookConfig.getWebhookId(), 1L);
        assertEquals(webhookConfig.getAction(), PullRequestAction.OPEN);
        assertEquals(webhookConfig.getRepositoryId(), 100L);
        assertEquals(webhookConfig.getRepositoryName(), "Test");
        assertEquals(webhookConfig.getRepositoryOrganization(), "LPVS");
        assertEquals(webhookConfig.getRepositoryUrl(), "https://github.com/samsung/lpvs");
        assertEquals(webhookConfig.getRepositoryLicense(), "MIT");
        assertEquals(webhookConfig.getHeadCommitSHA(), "2405d91eebb40e8841465908a0cd9200bba2da12");
        assertEquals(webhookConfig.getPullRequestUrl(), "https://github.com/Samsung/LPVS/pull/16");
        assertEquals(webhookConfig.getPullRequestFilesUrl(), "https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        assertEquals(webhookConfig.getPullRequestAPIUrl(), "https://github.com/api");
        assertEquals(webhookConfig.getPullRequestId(), 10L);
        assertEquals(webhookConfig.getPullRequestName(), "Test");
        assertEquals(webhookConfig.getUserId(), "BestUser");
        assertEquals(webhookConfig.getHubLink(), "https://some.link");
        assertEquals(webhookConfig.getBranch(), "main");
        assertEquals(webhookConfig.getPullRequestBranch(), "test");
        assertEquals(webhookConfig.getAttempts(), 10);
        assertEquals(webhookConfig.getDate(), date);
        assertEquals(webhookConfig.getReviewSystemType(), "scanner");
        assertEquals(webhookConfig.getReviewSystemName(), "scanoss");
        assertEquals(webhookConfig.getStatusCallbackUrl(), "https://some.url");
    }
}
