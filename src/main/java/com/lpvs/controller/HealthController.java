/**
 * Copyright (c) 2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.repository.LPVSQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling health check requests.
 * This class provides an endpoint to check the health of the service,
 * specifically returning the current length of the processing queue.
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    /**
     * Repository for accessing LPVSQueue entities to determine queue size.
     */
    private final LPVSQueueRepository queueRepository;

    /**
     * Constructor for HealthController.
     *
     * @param queueRepository Repository for accessing LPVSQueue entities.
     */
    @Autowired
    public HealthController(LPVSQueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    /**
     * Endpoint for checking the health of the service.
     * Returns the current number of items in the processing queue.
     *
     * @return A ResponseEntity containing a map with the queue length.
     */
    @GetMapping
    public ResponseEntity<Map<String, Long>> getHealthStatus() {
        long queueLength = queueRepository.count();
        Map<String, Long> response = new HashMap<>();
        response.put("queueLength", queueLength);
        log.info("Current queue length: {}", queueLength);
        return ResponseEntity.ok(response);
    }
}
