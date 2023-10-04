/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LPVSDetectedLicenseRepository extends JpaRepository<LPVSDetectedLicense, Long> {
    @Query(value = "select count(dl)>0 from LPVSDetectedLicense dl where dl.issue = True and dl.pullRequest = :pr")
    Boolean existsIssue(@Param("pr") LPVSPullRequest pr);

    List<LPVSDetectedLicense> findByPullRequest(LPVSPullRequest lpvsPullRequest);
    Page<LPVSDetectedLicense> findByPullRequest(LPVSPullRequest lpvsPullRequest, Pageable pageable);


    @Query(value = "select count(*) from LPVSDetectedLicense dl where dl.pullRequest = :pr and dl.license is not null")
    Long CountByDetectedLicenseWherePullRequestId(@Param("pr") LPVSPullRequest pr);

}
