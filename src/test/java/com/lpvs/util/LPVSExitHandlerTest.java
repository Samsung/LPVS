/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPVSExitHandlerTest {

    @Mock private ApplicationContext applicationContext;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExit() {
        LPVSExitHandler exitHandler = new LPVSExitHandler(applicationContext);
        int expectedExitCode = 123;
        int actualExitCode = exitHandler.exit(expectedExitCode);
        assertEquals(expectedExitCode, actualExitCode);
    }
}
