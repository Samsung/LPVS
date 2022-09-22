/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.fail;

public class ResponseWrapperTest {
    private static Logger LOG = LoggerFactory.getLogger(ResponseWrapperTest.class);

    final String test_value = "test_value";
    ResponseWrapper responseWrapper;

    @Test
    public void testResponseWrapperConstructor() {
        responseWrapper = new ResponseWrapper(test_value);

        String actual;
        try {
            Field message_field = responseWrapper.getClass().getDeclaredField("message");
            message_field.setAccessible(true);
            actual = (String) message_field.get(responseWrapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error("Java reflect exception at ResponseWrapperTest: " + e);
            fail();

            throw new RuntimeException();  // to get rid of "`actual` may be unassigned" warning
        }

        Assertions.assertEquals(test_value, actual);
    }
}
