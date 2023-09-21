/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSDetectedLicense;
import com.lpvs.entity.LPVSPullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LPVSDetectedLicenseRepository extends JpaRepository<LPVSDetectedLicense, Long> {
    @Query(value = "select count(dl)>0 from LPVSDetectedLicense dl where dl.issue = True and dl.pullRequest = :pr")
    Boolean existsIssue(@Param("pr") LPVSPullRequest pr);
}
