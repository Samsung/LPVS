/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import com.lpvs.entity.enums.LPVSPullRequestAction;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "queue", schema = "lpvs")
@Getter @Setter
public class LPVSQueue implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action")
    private LPVSPullRequestAction action;

    @Column(name = "attempts")
    private int attempts;

    @Column(name = "scan_date")
    private Date date;

    @Column(name = "user")
    private String userId;

    @Column(name = "review_system_type")
    private String reviewSystemType;

    @Column(name = "repository_url")
    private String repositoryUrl;

    @Column(name = "pull_request_url", columnDefinition = "LONGTEXT")
    private String pullRequestUrl;

    @Column(name = "pull_request_diff_url", columnDefinition = "LONGTEXT")
    private String pullRequestFilesUrl;

    @Column(name = "pull_request_api_url", columnDefinition = "LONGTEXT")
    private String pullRequestAPIUrl;

    @Column(name = "status_callback_url", columnDefinition = "LONGTEXT")
    private String statusCallbackUrl;

    @Column(name = "commit_sha", columnDefinition = "LONGTEXT")
    private String headCommitSHA;

    @Transient
    private String repositoryLicense;

    @Transient
    private String hubLink;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LPVSQueue that = (LPVSQueue) o;
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
