/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Represents an error response containing details about an encountered exception.
 * Instances of this class are typically returned as part of the application's error handling mechanism.
 *
 * <p>
 * The error response includes a timestamp, error message, error code, and HTTP status code.
 * </p>
 */
@Getter
public class ErrorResponse {

    /**
     * The timestamp indicating when the error occurred. It is set to the current date and time.
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * The descriptive message providing information about the encountered error.
     */
    private String message;

    /**
     * A specific code associated with the error, aiding in identifying and categorizing the error type.
     */
    private String code;

    /**
     * The HTTP status code indicating the response status related to the error.
     */
    private int status;

    /**
     * Constructs a new ErrorResponse with the specified message, code, and status.
     *
     * @param message The descriptive message providing information about the encountered error.
     * @param code    A specific code associated with the error, aiding in identifying and categorizing the error type.
     * @param status  The HTTP status code indicating the response status related to the error.
     */
    public ErrorResponse(String message, String code, int status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }
}
