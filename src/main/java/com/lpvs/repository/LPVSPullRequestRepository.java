/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSPullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface LPVSPullRequestRepository extends JpaRepository<LPVSPullRequest, Long> {
    @Query(value = "SELECT now();", nativeQuery = true)
    Date getNow();

    @Query(value = "select pr from LPVSPullRequest pr where pr.repositoryName like :name%")
    Page<LPVSPullRequest> findPullRequestByNameLike(@Param("name") String name, Pageable pageable);

    @Query(value = "select count(*) from LPVSPullRequest pr where pr.repositoryName like :name%")
    Long CountByPullRequestWhereNameLike(@Param("name") String name);

    @Query(value = "select pr from LPVSPullRequest pr where pr.sender = :name")
    Page<LPVSPullRequest> findBySender(@Param("name") String name, Pageable pageable);

    @Query(value = "select count(*) from LPVSPullRequest pr where pr.sender = :name")
    Long CountBySender(@Param("name") String name);

    @Query(value = "select pr from LPVSPullRequest pr where pr.pullRequestBase = :name")
    Page<LPVSPullRequest> findByPullRequestBase(@Param("name") String name, Pageable pageable);

    @Query(value = "select pr from LPVSPullRequest pr where pr.pullRequestBase = :name")
    List<LPVSPullRequest> findByPullRequestBase(@Param("name") String name);

    @Query(value = "select count(*) from LPVSPullRequest pr where pr.pullRequestBase = :name")
    Long CountByPullRequestBase(@Param("name") String name);

    @Query(value = "select pr from LPVSPullRequest pr where pr.sender = :name or pr.pullRequestHead = :name")
    Page<LPVSPullRequest> findBySenderOrPullRequestHead(@Param("name") String name, Pageable pageable);

    @Query(value = "select pr from LPVSPullRequest pr where pr.sender = :name or pr.pullRequestHead = :name")
    List<LPVSPullRequest> findBySenderOrPullRequestHead(@Param("name") String name);

    @Query(value = "select count(*) from LPVSPullRequest pr where pr.sender = :name or pr.pullRequestHead = :name")
    Long CountBySenderOrPullRequestHead(@Param("name") String name);


}
