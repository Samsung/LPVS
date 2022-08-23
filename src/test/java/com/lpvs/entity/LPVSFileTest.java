/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LPVSFileTest {

    LPVSFile lpvsFile;
    final long baseId = 1L;
    final String bseFileUrl = "baseFileUrl";
    final String baseFilePath = "baseFilePath";
    final String baseSnippetMatch = "baseSnippetMatch";
    final String baseMatchedLines = "baseMatchedLines";
    final String baseComponent = "baseComponent";

    @BeforeEach
    void setUp() {
        lpvsFile = new LPVSFile();
    }

    @Test
    public void getterSetterIdTest() {
        assertEquals(lpvsFile.getId(), null);
        lpvsFile.setId(baseId);
        assertNotEquals(lpvsFile.getId(), null);
        assertEquals(lpvsFile.getId(), baseId);
    }

    @Test
    public void getterSetterFileUrlTest() {
        assertEquals(lpvsFile.getFileUrl(), null);
        lpvsFile.setFileUrl(bseFileUrl);
        assertNotEquals(lpvsFile.getFileUrl(), null);
        assertEquals(lpvsFile.getFileUrl(), bseFileUrl);
    }

    @Test
    public void getterSetterFilePathTest() {
        assertEquals(lpvsFile.getFilePath(), null);
        lpvsFile.setFilePath(baseFilePath);
        assertNotEquals(lpvsFile.getFilePath(), null);
        assertEquals(lpvsFile.getFilePath(), baseFilePath);
    }

    @Test
    public void getterSetterSnippetMatchTest() {
        assertEquals(lpvsFile.getSnippetMatch(), null);
        lpvsFile.setSnippetMatch(baseSnippetMatch);
        assertNotEquals(lpvsFile.getSnippetMatch(), null);
        assertEquals(lpvsFile.getSnippetMatch(), baseSnippetMatch);
    }

    @Test
    public void getterSetterMatchedLinesTest() {
        assertEquals(lpvsFile.getMatchedLines(), null);
        lpvsFile.setMatchedLines(baseMatchedLines);
        assertNotEquals(lpvsFile.getMatchedLines(), null);
        assertEquals(lpvsFile.getMatchedLines(), baseMatchedLines);
    }

    @Test
    public void getterSetterComponentTest() {
        assertEquals(lpvsFile.getComponent(), null);
        lpvsFile.setComponent(baseComponent);
        assertNotEquals(lpvsFile.getComponent(), null);
        assertEquals(lpvsFile.getComponent(), baseComponent);
    }

    @Test
    public void convertLicensesToStringBaseTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseChecklistUrl = "checklistUrl";
        List<String> baseIncompatibleWith = Arrays.asList("incompatibleWith1", "incompatibleWith2", "incompatibleWith3");

        LPVSLicense lpvsLicense = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseChecklistUrl,
                baseIncompatibleWith);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(), "<a target=\"_blank\" href=\"checklistUrl\">spdxId</a> (access)");
    }

    @Test
    public void convertLicenseToStringCheckListUrlNullTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseChecklistUrl = "checklistUrl";
        List<String> baseIncompatibleWith = Arrays.asList("incompatibleWith1", "incompatibleWith2", "incompatibleWith3");

        LPVSLicense lpvsLicense1 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                null,
                baseIncompatibleWith);

        LPVSLicense lpvsLicense2 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                null,
                baseIncompatibleWith);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense1, lpvsLicense2));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(), "spdxId (access), spdxId (access)");
    }

    @Test
    public void convertLicenseToStringCheckListUrlTwoTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseChecklistUrl = "checklistUrl";
        List<String> baseIncompatibleWith = Arrays.asList("incompatibleWith1", "incompatibleWith2", "incompatibleWith3");

        LPVSLicense lpvsLicense1 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseChecklistUrl,
                baseIncompatibleWith);

        LPVSLicense lpvsLicense2 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseChecklistUrl, /*null,*/
                baseIncompatibleWith);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense1, lpvsLicense2));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(), "<a target=\"_blank\" href=\"checklistUrl\">spdxId</a> (access), <a target=\"_blank\" href=\"checklistUrl\">spdxId</a> (access)");
    }
}
