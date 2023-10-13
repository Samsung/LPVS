/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.results;

import com.lpvs.entity.result.LPVSResultFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class LPVSResultFileTest {

    private LPVSResultFile lpvsResultFile;
    private Long id = 1L;
    private String path = "example/file.txt";
    private String componentFileUrl = "https://example.com/file.txt";
    private String matchLine = "This is a matching line";
    private String matchValue = "LicenseA";
    private String status = "license.licenseUsage";
    private String licenseSpdx = "LicenseA";

    @BeforeEach
    public void setUp() {
        lpvsResultFile = new LPVSResultFile(id, path, componentFileUrl, matchLine, matchValue, status, licenseSpdx);
    }

    @Test
    public void testGetters() {
        // Test getters
        assertEquals(1L, lpvsResultFile.getId().longValue());
        assertEquals("example/file.txt", lpvsResultFile.getPath());
        assertEquals("https://example.com/file.txt", lpvsResultFile.getComponentFileUrl());
        assertEquals("This is a matching line", lpvsResultFile.getMatchLine());
        assertEquals("LicenseA", lpvsResultFile.getMatchValue());
        assertEquals("license.licenseUsage", lpvsResultFile.getStatus());
        assertEquals("LicenseA", lpvsResultFile.getLicenseSpdx());
    }

    @Test
    public void testInequality() {
        // Test inequality when objects are not the same
        LPVSResultFile file1 = new LPVSResultFile(1L, "example/file.txt", "https://example.com/file.txt",
            "This is a matching line", "LicenseA", "license.licenseUsage", "LicenseA");
        LPVSResultFile file2 = new LPVSResultFile(2L, "another/file.txt", "https://example.com/another.txt",
            "This is not a matching line", "LicenseB", "license.licenseUsage", "LicenseB");
        assertFalse(file1.equals(file2));
        assertFalse(file2.equals(file1));
    }
}
