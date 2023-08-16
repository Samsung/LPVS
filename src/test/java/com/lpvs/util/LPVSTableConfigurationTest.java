/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = LPVSTableConfiguration.class)
@TestPropertySource(properties = {
        "app.table.detectedLicenseName=mockDetectedLicenseName",
        "app.table.detectedLicenseShema=mockDetectedLicenseShema",
        "app.table.diffFileName=mockDiffFileName",
        "app.table.pullRequestsName=mockPullRequestsName",
        "app.table.queueName=mockQueueName"
})
public class LPVSTableConfigurationTest {

    @Autowired
    private LPVSTableConfiguration config;

    @Test
    public void testGetDetectedLicenseName() {
        assertEquals("mockDetectedLicenseName", config.getDetectedLicenseName());
    }

    @Test
    public void testGetDetectedLicenseShema() {
        assertEquals("mockDetectedLicenseShema", config.getDetectedLicenseShema());
    }

    @Test
    public void testGetDiffFileName() {
        assertEquals("mockDiffFileName", config.getDiffFileName());
    }

    @Test
    public void testGetPullRequestsName() {
        assertEquals("mockPullRequestsName", config.getPullRequestsName());
    }

    @Test
    public void testGetQueueName() {
        assertEquals("mockQueueName", config.getQueueName());
    }
}
