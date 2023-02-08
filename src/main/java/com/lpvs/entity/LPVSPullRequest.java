/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "pull_requests", schema = "lpvs")
@Getter @Setter
public class LPVSPullRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long pullRequestId;

    @Column(name = "scan_date")
    private Date date;

    @Column(name = "repository_name")
    private String repositoryName;

    @Column(name = "url", columnDefinition = "LONGTEXT")
    private String pullRequestUrl;

    @Column(name = "diff_url", columnDefinition = "LONGTEXT")
    private String pullRequestFilesUrl;

    @Column(name = "status")
    private String status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LPVSPullRequest that = (LPVSPullRequest) o;
        return date.equals(that.date) &&
                repositoryName.equals(that.repositoryName) &&
                pullRequestUrl.equals(that.pullRequestUrl) &&
                pullRequestFilesUrl.equals(that.pullRequestFilesUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, repositoryName, pullRequestUrl, pullRequestFilesUrl);
    }
}
