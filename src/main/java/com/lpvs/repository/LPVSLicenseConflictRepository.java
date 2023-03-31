/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSLicenseConflict;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LPVSLicenseConflictRepository extends CrudRepository<LPVSLicenseConflict, Long> {

    @Query(value = "SELECT * FROM lpvs_license_conflicts", nativeQuery = true)
    List<LPVSLicenseConflict> takeAllLicenseConflicts();

    @Query(value = "SELECT * FROM lpvs_license_conflicts WHERE (lpvs_license_conflicts.repository_license_id = :license1 AND lpvs_license_conflicts.conflict_license_id = :license2) " +
            "OR (lpvs_license_conflicts.repository_license_id = :license2 AND lpvs_license_conflicts.conflict_license_id = :license1) ORDER BY id DESC LIMIT 1", nativeQuery = true)
    LPVSLicenseConflict findLicenseConflict(@Param("license1") Long license1, @Param("license2") Long license2);
}
