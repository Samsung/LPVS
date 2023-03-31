/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import java.util.*;

import com.lpvs.entity.enums.LPVSVcs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LPVSFileTest {

    LPVSFile lpvsFile;
    final long baseId = 1L;
    final String baseFilePath = "baseFilePath";
    final String baseSnippetType = "baseSnippetType";
    final String baseSnippetMatch = "baseSnippetMatch";
    final String baseMatchedLines = "baseMatchedLines";
    final String baseComponentFilePath = "baseComponentFilePath";
    final String baseComponentName = "baseComponentName";
    final String baseComponentLines = "baseComponentLines";
    final String baseComponentUrl = "baseComponentUrl";
    final String baseComponentVersion = "baseComponentVersion";
    final String baseComponentVendor = "baseComponentVendor";

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
    public void getterSetterFilePathTest() {
        assertEquals(lpvsFile.getFilePath(), null);
        lpvsFile.setFilePath(baseFilePath);
        assertNotEquals(lpvsFile.getFilePath(), null);
        assertEquals(lpvsFile.getFilePath(), baseFilePath);
    }

    @Test
    public void getterSetterSnippetTypeTest() {
        assertEquals(lpvsFile.getSnippetType(), null);
        lpvsFile.setSnippetType(baseSnippetType);
        assertNotEquals(lpvsFile.getSnippetType(), null);
        assertEquals(lpvsFile.getSnippetType(), baseSnippetType);
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
    public void getterSetterComponentFilePathTest() {
        assertEquals(lpvsFile.getComponentFilePath(), null);
        lpvsFile.setComponentFilePath(baseComponentFilePath);
        assertNotEquals(lpvsFile.getComponentFilePath(), null);
        assertEquals(lpvsFile.getComponentFilePath(), baseComponentFilePath);
    }

    @Test
    public void getterSetterComponentNameTest() {
        assertEquals(lpvsFile.getComponentName(), null);
        lpvsFile.setComponentName(baseComponentName);
        assertNotEquals(lpvsFile.getComponentName(), null);
        assertEquals(lpvsFile.getComponentName(), baseComponentName);
    }

    @Test
    public void getterSetterComponentLinesTest() {
        assertEquals(lpvsFile.getComponentLines(), null);
        lpvsFile.setComponentLines(baseComponentLines);
        assertNotEquals(lpvsFile.getComponentLines(), null);
        assertEquals(lpvsFile.getComponentLines(), baseComponentLines);
    }

    @Test
    public void getterSetterComponentUrlTest() {
        assertEquals(lpvsFile.getComponentUrl(), null);
        lpvsFile.setComponentUrl(baseComponentUrl);
        assertNotEquals(lpvsFile.getComponentUrl(), null);
        assertEquals(lpvsFile.getComponentUrl(), baseComponentUrl);
    }

    @Test
    public void getterSetterComponentVersionTest() {
        assertEquals(lpvsFile.getComponentVersion(), null);
        lpvsFile.setComponentVersion(baseComponentVersion);
        assertNotEquals(lpvsFile.getComponentVersion(), null);
        assertEquals(lpvsFile.getComponentVersion(), baseComponentVersion);
    }

    @Test
    public void getterSetterComponentVendorTest() {
        assertEquals(lpvsFile.getComponentVendor(), null);
        lpvsFile.setComponentVendor(baseComponentVendor);
        assertNotEquals(lpvsFile.getComponentVendor(), null);
        assertEquals(lpvsFile.getComponentVendor(), baseComponentVendor);
    }

    @Test
    public void convertLicensesToStringBaseTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseAlternativeName = "licenseNameAlternative";
        final String baseChecklistUrl = "checklistUrl";

        LPVSLicense lpvsLicense = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseAlternativeName,
                baseChecklistUrl);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(LPVSVcs.GITHUB), "<a target=\"_blank\" href=\"checklistUrl\">spdxId</a> (access)");
    }

    @Test
    public void convertLicenseToStringCheckListUrlNullTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseAlternativeName = "licenseNameAlternative";
        final String baseChecklistUrl = "checklistUrl";
        List<String> baseIncompatibleWith = Arrays.asList("incompatibleWith1", "incompatibleWith2", "incompatibleWith3");

        LPVSLicense lpvsLicense1 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseAlternativeName,
                null);

        LPVSLicense lpvsLicense2 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseAlternativeName,
                null);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense1, lpvsLicense2));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(LPVSVcs.GITHUB), "spdxId (access), spdxId (access)");
    }

    @Test
    public void convertLicenseToStringCheckListUrlTwoTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseAlternativeName = "licenseNameAlternative";
        final String baseChecklistUrl = "checklistUrl";
        List<String> baseIncompatibleWith = Arrays.asList("incompatibleWith1", "incompatibleWith2", "incompatibleWith3");

        LPVSLicense lpvsLicense1 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseAlternativeName,
                baseChecklistUrl);

        LPVSLicense lpvsLicense2 = new LPVSLicense(baseLicenseId,
                baseLicenseName,
                baseSpdxId,
                baseAccess,
                baseAlternativeName,
                baseChecklistUrl);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense1, lpvsLicense2));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(LPVSVcs.GITHUB), "<a target=\"_blank\" href=\"checklistUrl\">spdxId</a> (access), <a target=\"_blank\" href=\"checklistUrl\">spdxId</a> (access)");
    }
}
