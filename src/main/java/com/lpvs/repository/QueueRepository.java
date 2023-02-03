/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.config.WebhookConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QueueRepository extends CrudRepository<WebhookConfig, Long> {

    @Query(value = "SELECT * FROM lpvs.queue", nativeQuery = true)
    List<WebhookConfig> getQueueList();
}
