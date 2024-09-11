/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import java.net.URISyntaxException;
import java.nio.file.Paths;
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
    final String baseAbsoluteFilePath = "baseAbsoluteFilePath";
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
    public void getterSetterAbsoluteFilePathTest() {
        assertEquals(lpvsFile.getAbsoluteFilePath(), null);
        lpvsFile.setAbsoluteFilePath(baseAbsoluteFilePath);
        assertNotEquals(lpvsFile.getAbsoluteFilePath(), null);
        assertEquals(lpvsFile.getAbsoluteFilePath(), baseAbsoluteFilePath);
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

        LPVSLicense lpvsLicense =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        baseChecklistUrl);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense));
        lpvsFile.setLicenses(licenses);
        assertEquals(
                lpvsFile.convertLicensesToString(LPVSVcs.GITHUB),
                "\n- ACCESS:\n  : <a target=\"_blank\" href=\"checklistUrl\">spdxId</a>\n");
    }

    @Test
    public void convertLicenseToStringCheckListUrlNullTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseAlternativeName = "licenseNameAlternative";

        LPVSLicense lpvsLicense1 =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        null);

        LPVSLicense lpvsLicense2 =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        null);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense1, lpvsLicense2));
        lpvsFile.setLicenses(licenses);
        assertEquals(
                lpvsFile.convertLicensesToString(LPVSVcs.GITHUB),
                "\n- ACCESS:\n  : spdxId\n  : spdxId\n");
    }

    @Test
    public void convertLicenseToStringCheckListUrlLicenseRef() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseAlternativeName = "licenseNameAlternative";

        LPVSLicense lpvsLicense3 =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        "LicenseRef-scancode-" + baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        null);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense3));
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(LPVSVcs.GITHUB), "\n- ACCESS:\n  : spdxId\n");

        licenses = new HashSet<>();
        lpvsFile.setLicenses(licenses);
        assertEquals(lpvsFile.convertLicensesToString(LPVSVcs.GERRIT), "");
    }

    @Test
    public void convertLicenseToStringCheckListUrlTwoTest() {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAccess = "access";
        final String baseAlternativeName = "licenseNameAlternative";
        final String baseChecklistUrl = "checklistUrl";

        LPVSLicense lpvsLicense1 =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        baseChecklistUrl);

        LPVSLicense lpvsLicense2 =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        baseChecklistUrl);

        Set<LPVSLicense> licenses = new HashSet<>(Arrays.asList(lpvsLicense1, lpvsLicense2));
        lpvsFile.setLicenses(licenses);
        assertEquals(
                lpvsFile.convertLicensesToString(LPVSVcs.GITHUB),
                "\n- ACCESS:\n  : <a target=\"_blank\" href=\"checklistUrl\">spdxId</a>\n  : <a target=\"_blank\" href=\"checklistUrl\">spdxId</a>\n");
    }

    @Test
    public void testConvertBytesToLinesNumbers() throws URISyntaxException {
        // Test when matchedLines starts with "BYTES:"
        LPVSFile validFile = new LPVSFile();

        validFile.setAbsoluteFilePath(
                Paths.get(
                                Objects.requireNonNull(
                                                getClass()
                                                        .getClassLoader()
                                                        .getResource("convert1.txt"))
                                        .toURI())
                        .toString());
        validFile.setMatchedLines("BYTES:0-3709:6492-7819");
        String result = validFile.convertBytesToLinesNumbers();
        assertEquals("1-107,208-248", result);

        // Test when matchedLines does not start with "BYTES:"
        validFile.setMatchedLines("5-10,15-20");
        result = validFile.convertBytesToLinesNumbers();
        assertEquals("5-10,15-20", result);
    }

    @Test
    public void testConvertBytesToLinesNumbers_N() throws URISyntaxException {
        LPVSFile invalidFile = new LPVSFile();
        // File does not exist
        invalidFile.setAbsoluteFilePath("convertX.txt");
        invalidFile.setMatchedLines("BYTES:0-3709:6492-7819");
        String result = invalidFile.convertBytesToLinesNumbers();
        assertEquals("", result);

        // Empty matched lines
        invalidFile.setMatchedLines(null);
        result = invalidFile.convertBytesToLinesNumbers();
        assertEquals("", result);

        invalidFile.setMatchedLines("");
        result = invalidFile.convertBytesToLinesNumbers();
        assertEquals("", result);
    }
}
