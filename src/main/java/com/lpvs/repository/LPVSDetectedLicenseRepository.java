/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSDetectedLicense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LPVSDetectedLicenseRepository extends JpaRepository<LPVSDetectedLicense, Long> {
}
