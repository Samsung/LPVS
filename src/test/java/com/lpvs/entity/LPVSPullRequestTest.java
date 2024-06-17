/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSPullRequestTest {

    LPVSPullRequest lpvsPullRequest;
    final long pullRequestId = 1234567890L;
    final Date date = new Date();
    final String user = "user";
    final String repositoryName = "repositoryName";
    final String pullRequestUrl = "pulRequestUrl";
    final String pullRequestFilesUrl = "pullRequestFileUrl";
    final String status = "status";
    final String pullRequestBase = "base";
    final String pullRequestHead = "head";
    final String sender = "sender";

    @BeforeEach
    void setUp() {
        lpvsPullRequest =
                new LPVSPullRequest(
                        pullRequestId,
                        date,
                        user,
                        repositoryName,
                        pullRequestUrl,
                        pullRequestFilesUrl,
                        status,
                        pullRequestHead,
                        pullRequestBase,
                        sender);
    }

    @Test
    public void gettersTest() {
        assertEquals(lpvsPullRequest.getId(), pullRequestId);
        assertEquals(lpvsPullRequest.getDate(), date);
        assertEquals(lpvsPullRequest.getUser(), user);
        assertEquals(lpvsPullRequest.getRepositoryName(), repositoryName);
        assertEquals(lpvsPullRequest.getPullRequestUrl(), pullRequestUrl);
        assertEquals(lpvsPullRequest.getPullRequestFilesUrl(), pullRequestFilesUrl);
        assertEquals(lpvsPullRequest.getStatus(), status);
        assertEquals(lpvsPullRequest.getPullRequestBase(), pullRequestBase);
        assertEquals(lpvsPullRequest.getPullRequestHead(), pullRequestHead);
    }

    @Test
    public void setPullRequestIdTest() {
        final long newActualValue = 0L;
        assertEquals(lpvsPullRequest.getId(), pullRequestId);
        lpvsPullRequest.setId(newActualValue);
        assertNotEquals(lpvsPullRequest.getId(), pullRequestId);
        assertEquals(lpvsPullRequest.getId(), newActualValue);
    }

    @Test
    public void setPullRequestDateTest() {
        final Date newActualValue = new Date(System.currentTimeMillis() - 3600 * 1000);
        assertEquals(lpvsPullRequest.getDate(), date);
        lpvsPullRequest.setDate(newActualValue);
        assertNotEquals(lpvsPullRequest.getDate(), date);
        assertEquals(lpvsPullRequest.getDate(), newActualValue);
    }

    @Test
    public void setPullRequestUserTest() {
        final String newActualUserName = "newUserName";
        assertEquals(lpvsPullRequest.getUser(), user);
        lpvsPullRequest.setUser(newActualUserName);
        assertNotEquals(lpvsPullRequest.getUser(), user);
        assertEquals(lpvsPullRequest.getUser(), newActualUserName);
    }

    @Test
    public void setPullRequestRepositoryTest() {
        final String newActualRepositoryName = "newRepositoryName";
        assertEquals(lpvsPullRequest.getRepositoryName(), repositoryName);
        lpvsPullRequest.setRepositoryName(newActualRepositoryName);
        assertNotEquals(lpvsPullRequest.getRepositoryName(), repositoryName);
        assertEquals(lpvsPullRequest.getRepositoryName(), newActualRepositoryName);
    }

    @Test
    public void setPullRequestUrlTest() {
        final String newActualUrl = "newUrl";
        assertEquals(lpvsPullRequest.getPullRequestUrl(), pullRequestUrl);
        lpvsPullRequest.setPullRequestUrl(newActualUrl);
        assertNotEquals(lpvsPullRequest.getPullRequestUrl(), pullRequestUrl);
        assertEquals(lpvsPullRequest.getPullRequestUrl(), newActualUrl);
    }

    @Test
    public void setPullRequestFilesUrlTest() {
        final String newActualFilesUrl = "newFilesUrl";
        assertEquals(lpvsPullRequest.getPullRequestFilesUrl(), pullRequestFilesUrl);
        lpvsPullRequest.setPullRequestFilesUrl(newActualFilesUrl);
        assertNotEquals(lpvsPullRequest.getPullRequestFilesUrl(), pullRequestFilesUrl);
        assertEquals(lpvsPullRequest.getPullRequestFilesUrl(), newActualFilesUrl);
    }

    @Test
    public void setPullRequestHead() {
        final String newPullRequestHead = "newPullRequestHead";
        assertEquals(lpvsPullRequest.getPullRequestHead(), pullRequestHead);
        lpvsPullRequest.setPullRequestHead(newPullRequestHead);
        assertNotEquals(lpvsPullRequest.getPullRequestHead(), pullRequestHead);
        assertEquals(lpvsPullRequest.getPullRequestHead(), newPullRequestHead);
    }

    @Test
    public void setPullRequestBase() {
        final String newPullRequestBase = "newPullRequestBase";
        assertEquals(lpvsPullRequest.getPullRequestBase(), pullRequestBase);
        lpvsPullRequest.setPullRequestBase(newPullRequestBase);
        assertNotEquals(lpvsPullRequest.getPullRequestBase(), pullRequestBase);
        assertEquals(lpvsPullRequest.getPullRequestBase(), newPullRequestBase);
    }

    @Test
    public void setPullRequestStatusTest() {
        final String newStatus = "newStatus";
        assertEquals(lpvsPullRequest.getStatus(), status);
        lpvsPullRequest.setStatus(newStatus);
        assertNotEquals(lpvsPullRequest.getStatus(), status);
        assertEquals(lpvsPullRequest.getStatus(), newStatus);
    }

    @Test
    public void testEquals() {
        LPVSPullRequest pr1 = new LPVSPullRequest();
        pr1.setDate(date);
        pr1.setRepositoryName(repositoryName);
        pr1.setPullRequestUrl(pullRequestUrl);
        pr1.setPullRequestFilesUrl(pullRequestFilesUrl);

        // Copy of the same LPVSPullRequest object
        LPVSPullRequest pr2 = new LPVSPullRequest();
        pr2.setDate(date);
        pr2.setRepositoryName(repositoryName);
        pr2.setPullRequestUrl(pullRequestUrl);
        pr2.setPullRequestFilesUrl(pullRequestFilesUrl);

        // Create LPVSPullRequest objects with different values for testing inequality
        LPVSPullRequest pr3 = new LPVSPullRequest();
        pr3.setDate(DateUtils.addDays(new Date(), -30)); /* initialize with different date */
        pr3.setRepositoryName(repositoryName);
        pr3.setPullRequestUrl(pullRequestUrl);
        pr3.setPullRequestFilesUrl(pullRequestFilesUrl);

        LPVSPullRequest pr4 = new LPVSPullRequest();
        pr4.setDate(date);
        pr4.setRepositoryName(repositoryName + "1"); /* initialize with different repositoryName */
        pr4.setPullRequestUrl(pullRequestUrl);
        pr4.setPullRequestFilesUrl(pullRequestFilesUrl);

        LPVSPullRequest pr5 = new LPVSPullRequest();
        pr5.setDate(date);
        pr5.setRepositoryName(repositoryName);
        pr5.setPullRequestUrl(pullRequestUrl + "1"); /* initialize with different pullRequestUrl */
        pr5.setPullRequestFilesUrl(pullRequestFilesUrl);

        LPVSPullRequest pr6 = new LPVSPullRequest();
        pr6.setDate(date);
        pr6.setRepositoryName(repositoryName);
        pr6.setPullRequestUrl(pullRequestUrl);
        pr6.setPullRequestFilesUrl(
                pullRequestFilesUrl + "1"); /* initialize with different pullRequestFilesUrl */

        assertTrue(pr1.equals(pr2)); // Objects are equal
        assertFalse(pr1.equals(pr3)); // Date is different
        assertFalse(pr1.equals(pr4)); // RepositoryName is different
        assertFalse(pr1.equals(pr5)); // PullRequestUrl is different
        assertFalse(pr1.equals(pr6)); // PullRequestFilesUrl is different

        assertFalse(pr1.equals(null)); // Null comparison
        assertFalse(pr1.equals(new Object())); // Different class comparison
    }
}
