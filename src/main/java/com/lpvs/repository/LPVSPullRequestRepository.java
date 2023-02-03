/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSPullRequest;
import org.springframework.data.repository.CrudRepository;

public interface LPVSPullRequestRepository extends CrudRepository<LPVSPullRequest, Long> {
}
