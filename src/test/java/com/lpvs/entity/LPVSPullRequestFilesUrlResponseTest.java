/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LPVSPullRequestFilesUrlResponseTest {

    @Test
    public void setPullRequestIdTest() {
        LPVSPullRequestFilesUrlResponse lpvsPullRequestFilesUrlResponse =
                new LPVSPullRequestFilesUrlResponse();
        final String newActualValue1 = "new value 1";
        final String newActualValue2 = "new value 2";
        lpvsPullRequestFilesUrlResponse.setDiff(newActualValue1);
        assertEquals(lpvsPullRequestFilesUrlResponse.getDiff(), newActualValue1);
        lpvsPullRequestFilesUrlResponse.setDiff(newActualValue2);
        assertNotEquals(lpvsPullRequestFilesUrlResponse.getDiff(), newActualValue1);
        assertEquals(lpvsPullRequestFilesUrlResponse.getDiff(), newActualValue2);
    }
}
