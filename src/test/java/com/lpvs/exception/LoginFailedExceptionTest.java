/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginFailedExceptionTest {

    @Test
    public void testLoginFailedExceptionConstructor() {
        String message = "Test Message";
        LoginFailedException exception = new LoginFailedException(message);

        // Check if the exception message is set correctly
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testLoginFailedExceptionDefaultMessage() {
        LoginFailedException exception = new LoginFailedException(null);

        // Check if the exception message is set to the default value
        assertEquals(null, exception.getMessage());
    }
}
