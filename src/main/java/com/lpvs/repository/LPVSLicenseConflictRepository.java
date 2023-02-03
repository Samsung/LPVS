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

    @Query(value = "SELECT * FROM lpvs.license_conflicts", nativeQuery = true)
    List<LPVSLicenseConflict> takeAllLicenseConflicts();

    @Query(value = "SELECT c FROM LPVSLicenseConflict c WHERE (c.repositoryLicense = :license1 AND c.conflictLicense = :license2) " +
            "OR (c.repositoryLicense = :license2 AND c.conflictLicense = :license1) LIMIT 1")
    LPVSLicenseConflict findLicenseConflict(@Param("license1") String license1, @Param("license2") String license2);
}
