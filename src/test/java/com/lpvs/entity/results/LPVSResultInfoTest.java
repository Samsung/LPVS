/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.results;

import com.lpvs.entity.result.LPVSResultInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSResultInfoTest {

    private LPVSResultInfo lpvsResultInfo;
    private Long id = 1L;
    private Date scanDate = new Date();
    private String repositoryName = "example/repository";
    private String status = "Success";
    private List<String> detectedLicenses = Arrays.asList("LicenseA", "LicenseB");

    @BeforeEach
    public void setUp() {
        lpvsResultInfo = new LPVSResultInfo(id, scanDate, repositoryName, status, detectedLicenses);
    }

    @Test
    public void testGetters() {
        // Test getters
        assertEquals(1L, lpvsResultInfo.getId().longValue());
        assertNotNull(lpvsResultInfo.getScanDate());
        assertEquals("example/repository", lpvsResultInfo.getRepositoryName());
        assertEquals("Success", lpvsResultInfo.getStatus());
        assertEquals(Arrays.asList("LicenseA", "LicenseB"), lpvsResultInfo.getDetectedLicenses());
    }

    @Test
    public void testInequality() {
        // Test inequality when objects are not the same
        LPVSResultInfo info1 = new LPVSResultInfo(1L, new Date(), "example/repository", "Success", Arrays.asList("LicenseA", "LicenseB"));
        LPVSResultInfo info2 = new LPVSResultInfo(2L, new Date(), "another/repository", "Failure", Arrays.asList("LicenseC"));
        assertFalse(info1.equals(info2));
        assertFalse(info2.equals(info1));
    }
}
