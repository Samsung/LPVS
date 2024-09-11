/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.repository;

import com.lpvs.entity.LPVSLicenseConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link LPVSLicenseConflict} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
@Repository
public interface LPVSLicenseConflictRepository extends JpaRepository<LPVSLicenseConflict, Long> {

    /**
     * Find a specific license conflict between two licenses.
     *
     * @param license1 The ID of the first license.
     * @param license2 The ID of the second license.
     * @return The latest {@link LPVSLicenseConflict} entity representing the conflict between the two licenses.
     */
    @Query(
            "SELECT lc FROM LPVSLicenseConflict lc "
                    + "WHERE (lc.repositoryLicense.licenseId = :license1 AND lc.conflictLicense.licenseId = :license2) "
                    + "OR (lc.repositoryLicense.licenseId = :license2 AND lc.conflictLicense.licenseId = :license1) "
                    + "ORDER BY lc.conflictId DESC "
                    + "LIMIT 1")
    LPVSLicenseConflict findLicenseConflict(
            @Param("license1") Long license1, @Param("license2") Long license2);
}
