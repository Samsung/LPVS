/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for controllers in the application.
 *
 * <p>
 * This class is annotated with {@code @ControllerAdvice}, making it a global exception handler
 * that can be shared across multiple controllers. It catches specific exceptions and generates
 * appropriate error responses with log messages.
 * </p><p>
 * The handled exceptions include {@code LoginFailedException} for unsuccessful login attempts,
 * {@code WrongAccessException} for unauthorized access, and {@code IllegalArgumentException}
 * for invalid arguments.
 * </p><p>
 * Log messages are generated for each exception type, and corresponding {@code errorResponse}
 * instances are created to convey error details in the response.
 * </p>
 *
 * @see com.lpvs.exception.LoginFailedException LoginFailedException
 * @see com.lpvs.exception.WrongAccessException WrongAccessException
 * @see java.lang.IllegalArgumentException IllegalArgumentException
 * @see com.lpvs.exception.ErrorResponse ErrorResponse
 */
@Slf4j
@ControllerAdvice
public class PageControllerAdvice {

    /**
     * The instance of {@code ErrorResponse} used to construct error responses for different
     * exception scenarios handled in this class.
     */
    private ErrorResponse errorResponse;

    /**
     * Handles {@code LoginFailedException} by logging the error and returning an
     * {@code errorResponse} with UNAUTHORIZED status.
     *
     * @param e The thrown {@code LoginFailedException}.
     * @return A {@code ResponseEntity} containing the error response and UNAUTHORIZED status.
     */
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> loginFailedHandle(LoginFailedException e) {
        log.error("loginFailed" + e.getMessage());
        errorResponse =
                new ErrorResponse(
                        e.getMessage(),
                        HttpStatus.UNAUTHORIZED.name(),
                        HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles {@code WrongAccessException} by logging the error and returning an
     * {@code errorResponse} with FORBIDDEN status.
     *
     * @param e The thrown {@code WrongAccessException}.
     * @return A {@code ResponseEntity} containing the error response and FORBIDDEN status.
     */
    @ExceptionHandler(WrongAccessException.class)
    public ResponseEntity<ErrorResponse> wrongAccessHandle(WrongAccessException e) {
        log.error("wrongAccess");
        errorResponse =
                new ErrorResponse(
                        e.getMessage(), HttpStatus.FORBIDDEN.name(), HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@code IllegalArgumentException} by logging the error and returning an
     * {@code errorResponse} with CONFLICT status.
     *
     * @param e The thrown {@code IllegalArgumentException}.
     * @return A {@code ResponseEntity} containing the error response and CONFLICT status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(IllegalArgumentException e) {
        log.error("duplicated key exception" + e.getMessage());
        errorResponse =
                new ErrorResponse(
                        e.getMessage(), HttpStatus.CONFLICT.name(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}
