/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import com.lpvs.entity.enums.LPVSPullRequestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSQueueTest {

    Date date = new Date();
    LPVSQueue webhookConfig;

    @BeforeEach
    void setUp() {
        webhookConfig = new LPVSQueue();
        webhookConfig.setId(1L);
        webhookConfig.setAction(LPVSPullRequestAction.OPEN);
        webhookConfig.setRepositoryLicense("MIT");
        webhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        webhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        webhookConfig.setPullRequestFilesUrl(
                "https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        webhookConfig.setPullRequestAPIUrl("https://github.com/api");
        webhookConfig.setStatusCallbackUrl("https://github.com/Samsung/LPVS/pull/16");
        webhookConfig.setUserId("BestUser");
        webhookConfig.setAttempts(10);
        webhookConfig.setDate(date);
        webhookConfig.setReviewSystemType("scanner");
    }

    @Test
    public void testWebhookConfigEqual() {
        LPVSQueue secondWebhookConfig = new LPVSQueue();
        secondWebhookConfig.setId(1L);
        secondWebhookConfig.setAction(LPVSPullRequestAction.OPEN);
        secondWebhookConfig.setRepositoryLicense("MIT");
        secondWebhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        secondWebhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        secondWebhookConfig.setPullRequestFilesUrl(
                "https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        secondWebhookConfig.setPullRequestAPIUrl("https://github.com/api");
        secondWebhookConfig.setStatusCallbackUrl("https://github.com/Samsung/LPVS/pull/16");
        secondWebhookConfig.setUserId("BestUser");
        secondWebhookConfig.setAttempts(10);
        secondWebhookConfig.setDate(date);
        secondWebhookConfig.setReviewSystemType("scanner");
        assertEquals(webhookConfig, secondWebhookConfig);
    }

    @Test
    public void testWebhookConfigNotEqual() {
        LPVSQueue secondWebhookConfig = new LPVSQueue();
        secondWebhookConfig.setId(1L);
        secondWebhookConfig.setAction(LPVSPullRequestAction.OPEN);
        secondWebhookConfig.setRepositoryLicense("MIT");
        secondWebhookConfig.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");
        secondWebhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/17");
        secondWebhookConfig.setPullRequestFilesUrl(
                "https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
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
        assertEquals(webhookConfig.getAction(), LPVSPullRequestAction.OPEN);
        assertEquals(webhookConfig.getRepositoryLicense(), "MIT");
        assertEquals(webhookConfig.getHeadCommitSHA(), "2405d91eebb40e8841465908a0cd9200bba2da12");
        assertEquals(webhookConfig.getPullRequestUrl(), "https://github.com/Samsung/LPVS/pull/16");
        assertEquals(
                webhookConfig.getPullRequestFilesUrl(),
                "https://github.com/Samsung/LPVS/pull/16/files#diff-9c5fb3d1");
        assertEquals(webhookConfig.getPullRequestAPIUrl(), "https://github.com/api");
        assertEquals(webhookConfig.getUserId(), "BestUser");
        assertEquals(webhookConfig.getAttempts(), 10);
        assertEquals(webhookConfig.getDate(), date);
        assertEquals(webhookConfig.getReviewSystemType(), "scanner");
        assertEquals(webhookConfig.getStatusCallbackUrl(), "https://github.com/Samsung/LPVS/pull/16");
    }

    @Test
    public void testEquals() {
        LPVSQueue queue1 = new LPVSQueue();
        queue1.setAttempts(5);
        queue1.setAction(LPVSPullRequestAction.OPEN);
        queue1.setUserId("BestUser");
        queue1.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        queue1.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");

        // Copy of the same LPVSQueue object
        LPVSQueue queue2 = new LPVSQueue();
        queue2.setAttempts(5);
        queue2.setAction(LPVSPullRequestAction.OPEN);
        queue2.setUserId("BestUser");
        queue2.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        queue2.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");

        // Create LPVSQueue objects with different values for testing inequality
        LPVSQueue queue3 = new LPVSQueue();
        queue3.setAttempts(10); /* initialize with different attempts */
        queue3.setAction(LPVSPullRequestAction.OPEN);
        queue3.setUserId("BestUser");
        queue3.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        queue3.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");

        LPVSQueue queue4 = new LPVSQueue();
        queue4.setAttempts(5);
        queue4.setAction(LPVSPullRequestAction.RESCAN); /* initialize with different action */
        queue4.setUserId("BestUser");
        queue4.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        queue4.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");

        LPVSQueue queue5 = new LPVSQueue();
        queue5.setAttempts(5);
        queue5.setAction(LPVSPullRequestAction.OPEN);
        queue5.setUserId("BestUser1"); /* initialize with different userId */
        queue5.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        queue5.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");

        LPVSQueue queue6 = new LPVSQueue();
        queue6.setAttempts(5);
        queue6.setAction(LPVSPullRequestAction.OPEN);
        queue6.setUserId("BestUser");
        queue6.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/17"); /* initialize with different pullRequestUrl */
        queue6.setHeadCommitSHA("2405d91eebb40e8841465908a0cd9200bba2da12");

        LPVSQueue queue7 = new LPVSQueue();
        queue7.setAttempts(5);
        queue7.setAction(LPVSPullRequestAction.OPEN);
        queue7.setUserId("BestUser");
        queue7.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/16");
        queue7.setHeadCommitSHA("1111111111111111111111111111111111"); /* initialize with different headCommitSHA */

        assertTrue(queue1.equals(queue2)); // Objects are equal
        assertFalse(queue1.equals(queue3)); // Attempts are different
        assertFalse(queue1.equals(queue4)); // Action is different
        assertFalse(queue1.equals(queue5)); // UserId is different
        assertFalse(queue1.equals(queue6)); // PullRequestUrl is different
        assertFalse(queue1.equals(queue7)); // HeadCommitSHA is different

        assertFalse(queue1.equals(null)); // Null comparison
        assertFalse(queue1.equals(new Object())); // Different class comparison
    }
}
