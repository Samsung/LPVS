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

/**
 * Repository interface for managing {@link LPVSPullRequest} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
public interface LPVSPullRequestRepository extends JpaRepository<LPVSPullRequest, Long> {
    /**
     * Get the current date and time from the database.
     *
     * @return The current date and time.
     */
    @Query(value = "SELECT now();", nativeQuery = true)
    Date getNow();

    /**
     * Find all pull requests with the specified base name, paginated.
     *
     * @param name     The name of the pull request base.
     * @param pageable The pagination information.
     * @return Page of {@link LPVSPullRequest} entities with the specified base name.
     */
    @Query(value = "select pr from LPVSPullRequest pr where pr.pullRequestBase = :name")
    Page<LPVSPullRequest> findByPullRequestBase(@Param("name") String name, Pageable pageable);

    /**
     * Find all pull requests with the specified base name.
     *
     * @param name The name of the pull request base.
     * @return List of {@link LPVSPullRequest} entities with the specified base name.
     */
    @Query(value = "select pr from LPVSPullRequest pr where pr.pullRequestBase = :name")
    List<LPVSPullRequest> findByPullRequestBase(@Param("name") String name);

    /**
     * Count the number of pull requests with the specified base name.
     *
     * @param name The name of the pull request base.
     * @return The count of pull requests with the specified base name.
     */
    @Query(value = "select count(*) from LPVSPullRequest pr where pr.pullRequestBase = :name")
    Long CountByPullRequestBase(@Param("name") String name);

    /**
     * Find all pull requests with the specified sender or pull request head, paginated.
     *
     * @param name     The name of the sender or pull request head.
     * @param pageable The pagination information.
     * @return Page of {@link LPVSPullRequest} entities with the specified sender or pull request head.
     */
    @Query(
            value =
                    "select pr from LPVSPullRequest pr where pr.sender = :name or pr.pullRequestHead = :name")
    Page<LPVSPullRequest> findBySenderOrPullRequestHead(
            @Param("name") String name, Pageable pageable);

    /**
     * Find all pull requests with the specified sender or pull request head.
     *
     * @param name The name of the sender or pull request head.
     * @return List of {@link LPVSPullRequest} entities with the specified sender or pull request head.
     */
    @Query(
            value =
                    "select pr from LPVSPullRequest pr where pr.sender = :name or pr.pullRequestHead = :name")
    List<LPVSPullRequest> findBySenderOrPullRequestHead(@Param("name") String name);

    /**
     * Count the number of pull requests with the specified sender or pull request head.
     *
     * @param name The name of the sender or pull request head.
     * @return The count of pull requests with the specified sender or pull request head.
     */
    @Query(
            value =
                    "select count(*) from LPVSPullRequest pr where pr.sender = :name or pr.pullRequestHead = :name")
    Long CountBySenderOrPullRequestHead(@Param("name") String name);
}
