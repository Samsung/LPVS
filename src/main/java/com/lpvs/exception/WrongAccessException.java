/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

/**
 * Exception thrown to indicate an attempt to access a resource without proper authorization.
 *
 * <p>
 * This exception is typically used when a user tries to access a resource for which they
 * do not have the necessary permissions or authentication.
 * </p><p>
 * Inherits from {@link RuntimeException} for unchecked exception handling.
 * </p>
 */
public class WrongAccessException extends RuntimeException {

    /**
     * Constructs a new WrongAccessException with the specified error message.
     *
     * @param message The detail message indicating the reason for the unauthorized access attempt.
     */
    public WrongAccessException(String message) {
        super(message);
    }
}
