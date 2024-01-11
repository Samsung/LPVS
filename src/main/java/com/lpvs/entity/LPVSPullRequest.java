/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a pull request in the LPVS system.
 * This class is mapped to the "pull_requests" table in the "lpvs" schema.
 */
@Entity
@Table(name = "pull_requests", schema = "lpvs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LPVSPullRequest implements Serializable {

    /**
     * The unique identifier for the pull request.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The date of the pull request.
     */
    @Column(name = "scan_date")
    private Date date;

    /**
     * The user associated with the pull request.
     */
    @Column(name = "user")
    private String user;

    /**
     * The name of the repository associated with the pull request.
     */
    @Column(name = "repository_name")
    private String repositoryName;

    /**
     * The URL of the pull request.
     */
    @Column(name = "url", columnDefinition = "LONGTEXT")
    private String pullRequestUrl;

    /**
     * The URL of the files in the pull request.
     */
    @Column(name = "diff_url", columnDefinition = "LONGTEXT")
    private String pullRequestFilesUrl;

    /**
     * The status of the pull request.
     */
    @Column(name = "status")
    private String status;

    /**
     * The head of the pull request.
     */
    @Column(name = "pull_request_head")
    private String pullRequestHead;

    /**
     * The base of the pull request.
     */
    @Column(name = "pull_request_base")
    private String pullRequestBase;

    /**
     * The sender of the pull request.
     */
    @Column(name = "sender")
    private String sender;

    /**
     * Checks if this pull request is equal to another object based on specific criteria.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LPVSPullRequest that = (LPVSPullRequest) o;
        return date.equals(that.date)
                && repositoryName.equals(that.repositoryName)
                && pullRequestUrl.equals(that.pullRequestUrl)
                && pullRequestFilesUrl.equals(that.pullRequestFilesUrl);
    }

    /**
     * Generates a hash code for the pull request based on specific attributes.
     *
     * @return The hash code for the pull request.
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, repositoryName, pullRequestUrl, pullRequestFilesUrl);
    }
}
