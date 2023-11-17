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

/**
 * Repository interface for managing {@link LPVSQueue} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
public interface LPVSQueueRepository extends JpaRepository<LPVSQueue, Long> {

    /**
     * Retrieve the list of all entities from the "queue" table.
     *
     * @return List of {@link LPVSQueue} entities representing the queue.
     */
    @Query(value = "SELECT * FROM queue", nativeQuery = true)
    List<LPVSQueue> getQueueList();
}
