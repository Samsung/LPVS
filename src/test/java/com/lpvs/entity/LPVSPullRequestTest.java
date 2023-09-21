/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LPVSPullRequestTest {

    LPVSPullRequest lpvsPullRequest;
    final long pullRequestId = 1234567890L;
    final Date date = new Date();
    final String user = "user";
    final String repositoryName = "repositoryName";
    final String pullRequestUrl = "pulRequestUrl";
    final String pullRequestFilesUrl = "pullRequestFileUrl";
    final String status = "status";
    final String sender = "user";
    final String owner = "user";

    @BeforeEach
    void setUp() {
        lpvsPullRequest = new LPVSPullRequest(pullRequestId,
                date,
                user,
                repositoryName,
                pullRequestUrl,
                pullRequestFilesUrl,
                status,
                sender,
                owner);
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
    public void setPullRequestStatusTest() {
        final String newStatus = "newStatus";
        assertEquals(lpvsPullRequest.getStatus(), status);
        lpvsPullRequest.setStatus(newStatus);
        assertNotEquals(lpvsPullRequest.getStatus(), status);
        assertEquals(lpvsPullRequest.getStatus(), newStatus);
    }
}
