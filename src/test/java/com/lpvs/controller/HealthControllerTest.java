/**
 * Copyright (c) 2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.repository.LPVSQueueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HealthControllerTest {

    @Mock private LPVSQueueRepository queueRepository;

    @InjectMocks private HealthController healthController;

    @Test
    public void testGetHealthStatus_Success_EmptyQueue() {
        when(queueRepository.count()).thenReturn(0L);

        ResponseEntity<Map<String, Long>> response = healthController.getHealthStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Long> responseBody = response.getBody();
        assertEquals(0L, responseBody.get("queueLength"));
    }

    @Test
    public void testGetHealthStatus_Success_WithItems() {
        long expectedQueueLength = 5L;
        when(queueRepository.count()).thenReturn(expectedQueueLength);

        ResponseEntity<Map<String, Long>> response = healthController.getHealthStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Long> responseBody = response.getBody();
        assertEquals(expectedQueueLength, responseBody.get("queueLength"));
    }
}
