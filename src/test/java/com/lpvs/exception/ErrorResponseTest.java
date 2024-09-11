/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {

    @Test
    public void testErrorResponseConstructor() {
        String message = "Test Message";
        String code = "TEST_CODE";
        int status = 404;
        ErrorResponse errorResponse = new ErrorResponse(message, code, status);

        // Check if the properties are set correctly
        assertEquals(message, errorResponse.getMessage());
        assertEquals(code, errorResponse.getCode());
        assertEquals(status, errorResponse.getStatus());

        // Check if the timestamp is not null
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    public void testErrorResponseTimestamp() {
        String message = "Another Test Message";
        String code = "ANOTHER_CODE";
        int status = 500;
        ErrorResponse errorResponse = new ErrorResponse(message, code, status);

        // Check if the timestamp is not null
        assertNotNull(errorResponse.getTimestamp());

        // Check if the timestamp is close to the current time (within a few milliseconds)
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime timestamp = errorResponse.getTimestamp();
        assertTrue(currentTime.isAfter(timestamp) || currentTime.isEqual(timestamp));
    }
}
