/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PageControllerAdviceTest {

    @InjectMocks
    private PageControllerAdvice pageControllerAdvice;

    @Mock
    private ErrorResponse errorResponse;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginFailedHandle() {
        String message = "Login failed message";
        LoginFailedException exception = new LoginFailedException(message);
        when(errorResponse.getMessage()).thenReturn(message);
        ResponseEntity<ErrorResponse> responseEntity = pageControllerAdvice.loginFailedHandle(exception);

        // Check if the response has the correct status, message, and code
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.name(), responseEntity.getBody().getCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), responseEntity.getBody().getStatus());
        assertNotNull(responseEntity.getBody().getTimestamp());
        assertTrue(responseEntity.getBody().getTimestamp().isBefore(LocalDateTime.now().plusSeconds(5))); // Adjust the time window as needed
    }

    @Test
    public void testWrongAccessHandle() {
        String message = "Access denied message";
        WrongAccessException exception = new WrongAccessException(message);
        when(errorResponse.getMessage()).thenReturn(message);
        ResponseEntity<ErrorResponse> responseEntity = pageControllerAdvice.wrongAccessHandle(exception);

        // Check if the response has the correct status, message, and code
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.FORBIDDEN.name(), responseEntity.getBody().getCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getBody().getStatus());
        assertNotNull(responseEntity.getBody().getTimestamp());
        assertTrue(responseEntity.getBody().getTimestamp().isBefore(LocalDateTime.now().plusSeconds(5))); // Adjust the time window as needed
    }

    @Test
    public void testHandleSQLException() {
        String message = "Conflict message";
        IllegalArgumentException exception = new IllegalArgumentException(message);
        when(errorResponse.getMessage()).thenReturn(message);
        ResponseEntity<ErrorResponse> responseEntity = pageControllerAdvice.handleSQLException(exception);

        // Check if the response has the correct status, message, and code
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(message, responseEntity.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.name(), responseEntity.getBody().getCode());
        assertEquals(HttpStatus.CONFLICT.value(), responseEntity.getBody().getStatus());
        assertNotNull(responseEntity.getBody().getTimestamp());
        assertTrue(responseEntity.getBody().getTimestamp().isBefore(LocalDateTime.now().plusSeconds(5))); // Adjust the time window as needed
    }
}
