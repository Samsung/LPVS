/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LPVSQueueRepository extends JpaRepository<LPVSQueue, Long> {

    @Query(value = "SELECT * FROM lpvs_queue", nativeQuery = true)
    List<LPVSQueue> getQueueList();
}
