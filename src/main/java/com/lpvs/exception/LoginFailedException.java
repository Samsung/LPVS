/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

/**
 * Exception thrown to indicate a failed login attempt.
 *
 * <p>
 * This exception is typically used when authentication fails, and the application needs
 * to communicate the failure reason to the user or handle it in a specific way.
 * </p><p>
 * Inherits from {@link RuntimeException} for unchecked exception handling.
 * </p>
 */
public class LoginFailedException extends RuntimeException {
    /**
     * Constructs a new LoginFailedException with the specified error message.
     *
     * @param message The detail message indicating the reason for the login failure.
     */
    public LoginFailedException(String message) {
        super(message);
    }
}
