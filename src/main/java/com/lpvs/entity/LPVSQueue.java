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

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Represents an item in the LPVS queue for processing pull requests.
 * This class is mapped to the "queue" table in the "lpvs" schema.
 */
@Entity
@Table(name = "queue", schema = "lpvs")
@Getter
@Setter
public class LPVSQueue implements Serializable {

    /**
     * The unique identifier for the queue item.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The action to be performed on the pull request.
     */
    @Column(name = "action")
    private LPVSPullRequestAction action;

    /**
     * The number of attempts made to process the item.
     */
    @Column(name = "attempts")
    private int attempts;

    /**
     * The date of the queue item.
     */
    @Column(name = "scan_date")
    private Date date;

    /**
     * The user identifier associated with the queue item.
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * The type of review system associated with the queue item.
     */
    @Column(name = "review_system_type")
    private String reviewSystemType;

    /**
     * The URL of the repository associated with the queue item.
     */
    @Column(name = "repository_url")
    private String repositoryUrl;

    /**
     * The URL of the pull request associated with the queue item.
     */
    @Column(name = "pull_request_url", columnDefinition = "LONGTEXT")
    private String pullRequestUrl;

    /**
     * The URL of the pull request files associated with the queue item.
     */
    @Column(name = "pull_request_diff_url", columnDefinition = "LONGTEXT")
    private String pullRequestFilesUrl;

    /**
     * The API URL of the pull request associated with the queue item.
     */
    @Column(name = "pull_request_api_url", columnDefinition = "LONGTEXT")
    private String pullRequestAPIUrl;

    /**
     * The URL for the status callback associated with the queue item.
     */
    @Column(name = "status_callback_url", columnDefinition = "LONGTEXT")
    private String statusCallbackUrl;

    /**
     * The SHA of the head commit associated with the queue item.
     */
    @Column(name = "commit_sha", columnDefinition = "LONGTEXT")
    private String headCommitSHA;

    /**
     * The head of the pull request associated with the queue item.
     */
    @Column(name = "pull_request_head")
    private String pullRequestHead;

    /**
     * The base of the pull request associated with the queue item.
     */
    @Column(name = "pull_request_base")
    private String pullRequestBase;

    /**
     * The sender of the pull request associated with the queue item.
     */
    @Column(name = "sender")
    private String sender;

    /**
     * Transient field representing the license information associated with the queue item.
     */
    @Transient private String repositoryLicense;

    /**
     * Transient field representing the hub link associated with the queue item.
     */
    @Transient private String hubLink;

    /**
     * Checks if this queue item is equal to another object based on specific criteria.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LPVSQueue that = (LPVSQueue) o;
        return attempts == that.attempts
                && action == that.action
                && Objects.equals(userId, that.userId)
                && pullRequestUrl.equals(that.pullRequestUrl)
                && Objects.equals(headCommitSHA, that.headCommitSHA);
    }

    /**
     * Generates a hash code for the queue item based on specific attributes.
     *
     * @return The hash code for the queue item.
     */
    @Override
    public int hashCode() {
        return Objects.hash(action, attempts, userId, pullRequestUrl, headCommitSHA);
    }
}
