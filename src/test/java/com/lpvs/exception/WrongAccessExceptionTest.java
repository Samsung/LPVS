/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WrongAccessExceptionTest {

    @Test
    public void testWrongAccessExceptionConstructor() {
        String message = "Test Message";
        WrongAccessException exception = new WrongAccessException(message);

        // Check if the exception message is set correctly
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testWrongAccessExceptionDefaultMessage() {
        WrongAccessException exception = new WrongAccessException(null);

        // Check if the exception message is set to the default value
        assertEquals(null, exception.getMessage());
    }
}
