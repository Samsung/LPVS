/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GradeTest {

    @Test
    public void testGradeValues() {
        assertEquals(Grade.SERIOUS, Grade.valueOf("SERIOUS"));
        assertEquals(Grade.HIGH, Grade.valueOf("HIGH"));
        assertEquals(Grade.MIDDLE, Grade.valueOf("MIDDLE"));
        assertEquals(Grade.LOW, Grade.valueOf("LOW"));
        assertEquals(Grade.NONE, Grade.valueOf("NONE"));
    }

    @Test
    public void testToString() {
        assertEquals("SERIOUS", Grade.SERIOUS.toString());
        assertEquals("HIGH", Grade.HIGH.toString());
        assertEquals("MIDDLE", Grade.MIDDLE.toString());
        assertEquals("LOW", Grade.LOW.toString());
        assertEquals("NONE", Grade.NONE.toString());
    }
}
