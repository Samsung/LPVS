/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.config;

import com.lpvs.entity.enums.PullRequestAction;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "queue", schema = "lpvs")
public class WebhookConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "action")
    PullRequestAction action;

    @Column(name = "attempts")
    int attempts;

    @Column(name = "scan_date")
    Date date;

    @Column(name = "user")
    String userId;

    @Column(name = "review_system_type")
    String reviewSystemType;

    @Column(name = "pull_request_url", columnDefinition = "LONGTEXT")
    String pullRequestUrl;

    @Column(name = "pull_request_files_url", columnDefinition = "LONGTEXT")
    String pullRequestFilesUrl;

    @Column(name = "pull_request_api_url", columnDefinition = "LONGTEXT")
    String pullRequestAPIUrl;

    @Column(name = "commit_sha", columnDefinition = "LONGTEXT")
    String headCommitSHA;

    @Transient
    String repositoryLicense;

    public WebhookConfig() { }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public PullRequestAction getAction() {
        return action;
    }

    public void setAction(PullRequestAction action) {
        this.action = action;
    }

    public int getAttempts() { return attempts; }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReviewSystemType() {
        return reviewSystemType;
    }

    public void setReviewSystemType(String reviewSystemType) {
        this.reviewSystemType = reviewSystemType;
    }

    public String getPullRequestUrl() {
        return pullRequestUrl;
    }

    public void setPullRequestUrl(String pullRequestUrl) {
        this.pullRequestUrl = pullRequestUrl;
    }

    public String getPullRequestFilesUrl() {
        return pullRequestFilesUrl;
    }

    public void setPullRequestFilesUrl(String pullRequestFilesUrl) {
        this.pullRequestFilesUrl = pullRequestFilesUrl;
    }

    public String getPullRequestAPIUrl() { return pullRequestAPIUrl; }

    public void setPullRequestAPIUrl(String pullRequestAPIUrl) { this.pullRequestAPIUrl = pullRequestAPIUrl; }

    public String getHeadCommitSHA() { return headCommitSHA; }

    public void setHeadCommitSHA(String headCommitSHA) { this.headCommitSHA = headCommitSHA; }

    public String getRepositoryLicense() { return repositoryLicense; }

    public void setRepositoryLicense(String repositoryLicense) { this.repositoryLicense = repositoryLicense; }

    @Override
    public String toString(){
        return "WebhookConfig [action = " + getAction() + "; pull request = " + getPullRequestUrl() + "; commit SHA = " + getHeadCommitSHA() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookConfig that = (WebhookConfig) o;
        return attempts == that.attempts &&
                action == that.action &&
                Objects.equals(userId, that.userId) &&
                pullRequestUrl.equals(that.pullRequestUrl) &&
                Objects.equals(headCommitSHA, that.headCommitSHA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, attempts, userId, pullRequestUrl, headCommitSHA);
    }
}
