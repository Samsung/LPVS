/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
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

import java.util.List;

/**
 * Repository interface for managing {@link LPVSLicenseConflict} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
@Repository
public interface LPVSLicenseConflictRepository extends JpaRepository<LPVSLicenseConflict, Long> {

    /**
     * Retrieve all license conflicts from the database.
     *
     * @return List of {@link LPVSLicenseConflict} entities representing license conflicts.
     */
    @Query(value = "SELECT * FROM license_conflicts", nativeQuery = true)
    List<LPVSLicenseConflict> takeAllLicenseConflicts();

    /**
     * Find a specific license conflict between two licenses.
     *
     * @param license1 The ID of the first license.
     * @param license2 The ID of the second license.
     * @return The latest {@link LPVSLicenseConflict} entity representing the conflict between the two licenses.
     */
    @Query(
            value =
                    "SELECT * FROM license_conflicts WHERE (license_conflicts.repository_license_id = :license1 AND license_conflicts.conflict_license_id = :license2) "
                            + "OR (license_conflicts.repository_license_id = :license2 AND license_conflicts.conflict_license_id = :license1) ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    LPVSLicenseConflict findLicenseConflict(
            @Param("license1") Long license1, @Param("license2") Long license2);
}
