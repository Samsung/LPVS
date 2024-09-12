/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.repository;

import com.lpvs.entity.LPVSDetectedLicense;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSPullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for managing {@link LPVSDetectedLicense} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
public interface LPVSDetectedLicenseRepository extends JpaRepository<LPVSDetectedLicense, Long> {
    /**
     * Check if there is at least one detected license associated with a specific pull request and marked as an issue.
     *
     * @param pr The {@link LPVSPullRequest} to check.
     * @return {@code true} if at least one detected license with an issue is associated with the pull request, {@code false} otherwise.
     */
    Boolean existsByIssueIsTrueAndPullRequest(@Param("pr") LPVSPullRequest pr);

    /**
     * Find all detected licenses associated with a specific pull request.
     *
     * @param lpvsPullRequest The {@link LPVSPullRequest} to retrieve detected licenses for.
     * @return List of {@link LPVSDetectedLicense} associated with the pull request.
     */
    List<LPVSDetectedLicense> findByPullRequest(LPVSPullRequest lpvsPullRequest);

    /**
     * Find a paginated list of detected licenses associated with a specific pull request.
     *
     * @param lpvsPullRequest The {@link LPVSPullRequest} to retrieve detected licenses for.
     * @param pageable        The pagination information.
     * @return Page of {@link LPVSDetectedLicense} associated with the pull request.
     */
    Page<LPVSDetectedLicense> findByPullRequest(LPVSPullRequest lpvsPullRequest, Pageable pageable);

    /**
     * Count the number of detected licenses associated with a specific pull request where the license is not null.
     *
     * @param pr The {@link LPVSPullRequest} to count detected licenses for.
     * @return The count of detected licenses where the license is not null.
     */
    Long countByPullRequestAndLicenseIsNotNull(@Param("pr") LPVSPullRequest pr);

    /**
     * Find distinct licenses associated with a specific pull request.
     *
     * @param pr The {@link LPVSPullRequest} to retrieve distinct licenses for.
     * @return List of distinct {@link LPVSLicense} associated with the pull request.
     */
    List<LPVSLicense> findDistinctLicenseByPullRequest(@Param("pr") LPVSPullRequest pr);

    /**
     * Find detected licenses associated with a specific pull request where the license is not null.
     *
     * @param pr The {@link LPVSPullRequest} to retrieve detected licenses for.
     * @return List of {@link LPVSDetectedLicense} associated with the pull request where the license is not null.
     */
    List<LPVSDetectedLicense> findByPullRequestAndLicenseIsNotNull(@Param("pr") LPVSPullRequest pr);
}
