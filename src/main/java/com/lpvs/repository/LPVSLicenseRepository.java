/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.repository;

import com.lpvs.entity.LPVSLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing {@link LPVSLicense} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
@Repository
public interface LPVSLicenseRepository extends JpaRepository<LPVSLicense, Long> {

    /**
     * Search for a license by SPDX identifier.
     *
     * @param spdxId The SPDX identifier of the license.
     * @return The latest {@link LPVSLicense} entity with the specified SPDX identifier.
     */
    LPVSLicense findFirstBySpdxIdOrderByLicenseIdDesc(@Param("spdxId") String spdxId);

    /**
     * Search for a license by alternative license names.
     *
     * @param licenseName The alternative license name to search for.
     * @return The latest {@link LPVSLicense} entity with the specified alternative license name.
     */
    @Query(
            "SELECT l FROM LPVSLicense l WHERE (CONCAT(',', l.alternativeNames, ',') LIKE CONCAT('%,', :licenseName, ',%')) "
                    + "ORDER BY l.licenseId DESC LIMIT 1")
    LPVSLicense searchByAlternativeLicenseNames(@Param("licenseName") String licenseName);

    /**
     * Retrieve all SPDX identifiers of licenses from the database.
     *
     * @return List of SPDX identifiers as Strings.
     */
    @Query(value = "select l.spdxId from LPVSLicense l")
    List<String> findAllSpdxId();
}
