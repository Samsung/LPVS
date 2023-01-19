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
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LPVSLicenseConflictRepository extends CrudRepository<LPVSLicenseConflict, Long> {

    @Query(value = "SELECT * FROM lpvs.license_conflicts", nativeQuery = true)
    List<LPVSLicenseConflict> takeAllLicenseConflicts();
}
