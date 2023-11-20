/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

/**
 * This package contains custom exception classes used in the application to represent various error scenarios.
 * These exceptions extend the standard {@link java.lang.RuntimeException} class, making them unchecked
 * exceptions that can be thrown and propagated throughout the application.
 *
 * <p>
 * The exceptions in this package are designed to cover specific error conditions, such as failed login attempts
 * ({@link com.lpvs.exception.LoginFailedException}), unauthorized access attempts
 * ({@link com.lpvs.exception.WrongAccessException}), and other application-specific error situations.
 * </p><p>
 * Each exception class includes a constructor that allows the specification of a detailed error message,
 * providing additional context about the reason for the exception.
 * </p><p>
 * These custom exceptions are often used in conjunction with a global exception handling mechanism to generate
 * consistent and meaningful error responses for the end-users and to facilitate proper error logging.
 * </p>
 */
package com.lpvs.exception;
