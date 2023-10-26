/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.results;

import com.lpvs.entity.result.LPVSResult;
import com.lpvs.entity.result.LPVSResultFile;
import com.lpvs.entity.result.LPVSResultInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LPVSResultTest {

    private LPVSResult lpvsResult;
    private List<LPVSResultFile> resultFileList = new ArrayList<>();
    private LPVSResultInfo resultInfo = new LPVSResultInfo(null, null, null, null, null);
    private Long count = 42L;
    private Map<String, Integer> licenseCountMap = new HashMap<>();
    private String pullNumber = "123";
    private Boolean hasIssue = true;

    @BeforeEach
    public void setUp() {
        licenseCountMap.put("LicenseA", 1);
        lpvsResult =
                new LPVSResult(
                        resultFileList, resultInfo, count, licenseCountMap, pullNumber, hasIssue);
    }

    @Test
    public void testGetters() {
        // Test getters
        assertEquals(resultFileList, lpvsResult.getLpvsResultFileList());
        assertEquals(resultInfo, lpvsResult.getLpvsResultInfo());
        assertEquals(count, lpvsResult.getCount());
        assertEquals(licenseCountMap, lpvsResult.getLicenseCountMap());
        assertEquals(pullNumber, lpvsResult.getPullNumber());
        assertEquals(hasIssue, lpvsResult.getHasIssue());
    }

    @Test
    public void testInequality() {
        // Test inequality when objects are not the same
        LPVSResult result1 =
                new LPVSResult(
                        resultFileList, resultInfo, count, licenseCountMap, pullNumber, hasIssue);
        LPVSResult result2 =
                new LPVSResult(resultFileList, resultInfo, count, licenseCountMap, "456", hasIssue);
        assertFalse(result1.equals(result2));
        assertFalse(result2.equals(result1));
    }

    @Test
    public void testDifferentClassesEquality() {
        // Test equality with an object of a different class
        LPVSResult result1 =
                new LPVSResult(
                        resultFileList, resultInfo, count, licenseCountMap, pullNumber, hasIssue);
        Object result2 = new Object();
        assertFalse(result1.equals(result2));
    }

    @Test
    public void testInequalityWithNullValues() {
        // Test inequality when some values are null
        LPVSResult result1 =
                new LPVSResult(
                        resultFileList, resultInfo, count, licenseCountMap, pullNumber, hasIssue);
        LPVSResult result2 = new LPVSResult(null, null, null, null, null, null);
        assertFalse(result1.equals(result2));
        assertFalse(result2.equals(result1));
    }
}
