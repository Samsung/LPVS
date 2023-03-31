/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSPullRequestActionTest {

    @Test
    public void testConvertFrom() {
        assertEquals(LPVSPullRequestAction.convertFrom("opened"), LPVSPullRequestAction.OPEN);
        assertEquals(LPVSPullRequestAction.convertFrom("reopened"), LPVSPullRequestAction.REOPEN);
        assertEquals(LPVSPullRequestAction.convertFrom("closed"), LPVSPullRequestAction.CLOSE);
        assertEquals(LPVSPullRequestAction.convertFrom("synchronize"), LPVSPullRequestAction.UPDATE);
        assertEquals(LPVSPullRequestAction.convertFrom("rescan"), LPVSPullRequestAction.RESCAN);

        assertNotEquals(LPVSPullRequestAction.convertFrom("random_name"), LPVSPullRequestAction.OPEN);
        assertNotEquals(LPVSPullRequestAction.convertFrom("random_name"), LPVSPullRequestAction.REOPEN);
        assertNotEquals(LPVSPullRequestAction.convertFrom("random_name"), LPVSPullRequestAction.CLOSE);
        assertNotEquals(LPVSPullRequestAction.convertFrom("random_name"), LPVSPullRequestAction.UPDATE);
        assertNotEquals(LPVSPullRequestAction.convertFrom("random_name"), LPVSPullRequestAction.RESCAN);

        assertNull(LPVSPullRequestAction.convertFrom("random_name"));

    }

    @Test
    public void testGetPullRequestAction() {
        assertEquals(LPVSPullRequestAction.OPEN.getPullRequestAction(), "opened");
        assertEquals(LPVSPullRequestAction.REOPEN.getPullRequestAction(), "reopened");
        assertEquals(LPVSPullRequestAction.CLOSE.getPullRequestAction(), "closed");
        assertEquals(LPVSPullRequestAction.UPDATE.getPullRequestAction(), "synchronize");
        assertEquals(LPVSPullRequestAction.RESCAN.getPullRequestAction(), "rescan");
    }
}
