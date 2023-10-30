/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
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

@Repository
public interface LPVSLicenseRepository extends JpaRepository<LPVSLicense, Long> {

    @Query(value = "SELECT * FROM licenses", nativeQuery = true)
    List<LPVSLicense> takeAllLicenses();

    @Query(
            value =
                    "SELECT * FROM licenses WHERE licenses.license_spdx = :spdxId ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    LPVSLicense searchBySpdxId(@Param("spdxId") String spdxId);

    @Query(
            value =
                    "SELECT * FROM licenses WHERE licenses.license_alternative_names LIKE %:licenseName% ORDER BY id DESC LIMIT 1",
            nativeQuery = true)
    LPVSLicense searchByAlternativeLicenseNames(@Param("licenseName") String licenseName);

    @Query(value = "select licenses.spdxId from LPVSLicense licenses")
    List<String> takeAllSpdxId();
}
