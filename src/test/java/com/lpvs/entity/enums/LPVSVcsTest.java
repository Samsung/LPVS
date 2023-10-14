/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LPVSVcsTest {

    @Test
    public void testGetVcs() {
        assertEquals("github", LPVSVcs.GITHUB.getVcs());
        assertEquals("swarm", LPVSVcs.SWARM.getVcs());
        assertEquals("gerrit", LPVSVcs.GERRIT.getVcs());
        assertEquals("gitlab", LPVSVcs.GITLAB.getVcs());
    }

    @Test
    public void testToString() {
        assertEquals("github", LPVSVcs.GITHUB.toString());
        assertEquals("swarm", LPVSVcs.SWARM.toString());
        assertEquals("gerrit", LPVSVcs.GERRIT.toString());
        assertEquals("gitlab", LPVSVcs.GITLAB.toString());
    }

    @Test
    public void testConvertFrom() {
        assertEquals(LPVSVcs.GITHUB, LPVSVcs.convertFrom("github"));
        assertEquals(LPVSVcs.SWARM, LPVSVcs.convertFrom("swarm"));
        assertEquals(LPVSVcs.GERRIT, LPVSVcs.convertFrom("gerrit"));
        assertEquals(LPVSVcs.GITLAB, LPVSVcs.convertFrom("gitlab"));
        assertNull(LPVSVcs.convertFrom("unknown"));
    }
}
