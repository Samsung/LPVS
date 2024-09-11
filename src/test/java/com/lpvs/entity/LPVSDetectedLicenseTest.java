/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSDetectedLicenseTest {

    LPVSDetectedLicense detectedLicense;

    @BeforeEach
    void setUp() {
        detectedLicense = new LPVSDetectedLicense();
        detectedLicense.setId(1L);
        detectedLicense.setFilePath("sample/file/path");
        detectedLicense.setType("sampleType");
        detectedLicense.setMatch("sampleMatch");
        detectedLicense.setLines("sampleLines");
        detectedLicense.setComponentFilePath("sampleComponentFilePath");
        detectedLicense.setComponentFileUrl("sampleComponentFileUrl");
        detectedLicense.setComponentName("sampleComponentName");
        detectedLicense.setComponentLines("sampleComponentLines");
        detectedLicense.setComponentUrl("sampleComponentUrl");
        detectedLicense.setComponentVersion("sampleComponentVersion");
        detectedLicense.setComponentVendor("sampleComponentVendor");
        detectedLicense.setIssue(true);
    }

    @Test
    public void getterSetterIdTest() {
        assertEquals(detectedLicense.getId(), 1L);
        detectedLicense.setId(2L);
        assertNotEquals(detectedLicense.getId(), 1L);
        assertEquals(detectedLicense.getId(), 2L);
    }

    @Test
    public void constructorAndGettersTest() {
        assertEquals(1L, detectedLicense.getId());
        assertEquals("sample/file/path", detectedLicense.getFilePath());
        assertEquals("sampleType", detectedLicense.getType());
        assertEquals("sampleMatch", detectedLicense.getMatch());
        assertEquals("sampleLines", detectedLicense.getLines());
        assertEquals("sampleComponentFilePath", detectedLicense.getComponentFilePath());
        assertEquals("sampleComponentFileUrl", detectedLicense.getComponentFileUrl());
        assertEquals("sampleComponentName", detectedLicense.getComponentName());
        assertEquals("sampleComponentLines", detectedLicense.getComponentLines());
        assertEquals("sampleComponentUrl", detectedLicense.getComponentUrl());
        assertEquals("sampleComponentVersion", detectedLicense.getComponentVersion());
        assertEquals("sampleComponentVendor", detectedLicense.getComponentVendor());
        assertEquals(true, detectedLicense.getIssue());
    }

    @Test
    public void setFilePathTest() {
        final String newFilePath = "newFilePath";
        assertEquals("sample/file/path", detectedLicense.getFilePath());
        detectedLicense.setFilePath(newFilePath);
        assertNotEquals("sample/file/path", detectedLicense.getFilePath());
        assertEquals(newFilePath, detectedLicense.getFilePath());
    }

    @Test
    public void setTypeTest() {
        final String newType = "newType";
        assertEquals("sampleType", detectedLicense.getType());
        detectedLicense.setType(newType);
        assertNotEquals("sampleType", detectedLicense.getType());
        assertEquals(newType, detectedLicense.getType());
    }

    @Test
    public void setMatchTest() {
        final String newMatch = "newMatch";
        assertEquals("sampleMatch", detectedLicense.getMatch());
        detectedLicense.setMatch(newMatch);
        assertNotEquals("sampleMatch", detectedLicense.getMatch());
        assertEquals(newMatch, detectedLicense.getMatch());
    }

    @Test
    public void setLinesTest() {
        final String newLines = "newLines";
        assertEquals("sampleLines", detectedLicense.getLines());
        detectedLicense.setLines(newLines);
        assertNotEquals("sampleLines", detectedLicense.getLines());
        assertEquals(newLines, detectedLicense.getLines());
    }

    @Test
    public void setComponentFilePathTest() {
        final String newComponentFilePath = "newComponentFilePath";
        assertEquals("sampleComponentFilePath", detectedLicense.getComponentFilePath());
        detectedLicense.setComponentFilePath(newComponentFilePath);
        assertNotEquals("sampleComponentFilePath", detectedLicense.getComponentFilePath());
        assertEquals(newComponentFilePath, detectedLicense.getComponentFilePath());
    }

    @Test
    public void setComponentFileUrlTest() {
        final String newComponentFileUrl = "newComponentFileUrl";
        assertEquals("sampleComponentFileUrl", detectedLicense.getComponentFileUrl());
        detectedLicense.setComponentFileUrl(newComponentFileUrl);
        assertNotEquals("sampleComponentFileUrl", detectedLicense.getComponentFileUrl());
        assertEquals(newComponentFileUrl, detectedLicense.getComponentFileUrl());
    }

    @Test
    public void setComponentNameTest() {
        final String newComponentName = "newComponentName";
        assertEquals("sampleComponentName", detectedLicense.getComponentName());
        detectedLicense.setComponentName(newComponentName);
        assertNotEquals("sampleComponentName", detectedLicense.getComponentName());
        assertEquals(newComponentName, detectedLicense.getComponentName());
    }

    @Test
    public void setComponentLinesTest() {
        final String newComponentLines = "newComponentLines";
        assertEquals("sampleComponentLines", detectedLicense.getComponentLines());
        detectedLicense.setComponentLines(newComponentLines);
        assertNotEquals("sampleComponentLines", detectedLicense.getComponentLines());
        assertEquals(newComponentLines, detectedLicense.getComponentLines());
    }

    @Test
    public void setComponentUrlTest() {
        final String newComponentUrl = "newComponentUrl";
        assertEquals("sampleComponentUrl", detectedLicense.getComponentUrl());
        detectedLicense.setComponentUrl(newComponentUrl);
        assertNotEquals("sampleComponentUrl", detectedLicense.getComponentUrl());
        assertEquals(newComponentUrl, detectedLicense.getComponentUrl());
    }

    @Test
    public void setComponentVersionTest() {
        final String newComponentVersion = "newComponentVersion";
        assertEquals("sampleComponentVersion", detectedLicense.getComponentVersion());
        detectedLicense.setComponentVersion(newComponentVersion);
        assertNotEquals("sampleComponentVersion", detectedLicense.getComponentVersion());
        assertEquals(newComponentVersion, detectedLicense.getComponentVersion());
    }

    @Test
    public void setComponentVendorTest() {
        final String newComponentVendor = "newComponentVendor";
        assertEquals("sampleComponentVendor", detectedLicense.getComponentVendor());
        detectedLicense.setComponentVendor(newComponentVendor);
        assertNotEquals("sampleComponentVendor", detectedLicense.getComponentVendor());
        assertEquals(newComponentVendor, detectedLicense.getComponentVendor());
    }

    @Test
    public void setIssueTest() {
        final boolean newIssue = false;
        assertEquals(true, detectedLicense.getIssue());
        detectedLicense.setIssue(newIssue);
        assertNotEquals(true, detectedLicense.getIssue());
        assertEquals(newIssue, detectedLicense.getIssue());
    }

    @Test
    public void setPullRequestTest() {
        LPVSPullRequest pullRequest = new LPVSPullRequest();
        assertNull(detectedLicense.getPullRequest());
        detectedLicense.setPullRequest(pullRequest);
        assertNotNull(detectedLicense.getPullRequest());
    }

    @Test
    public void setLicenseTest() {
        LPVSLicense license = new LPVSLicense();
        assertNull(detectedLicense.getLicense());
        detectedLicense.setLicense(license);
        assertNotNull(detectedLicense.getLicense());
    }

    @Test
    public void setLicenseConflictTest() {
        LPVSLicenseConflict licenseConflict = new LPVSLicenseConflict();
        assertNull(detectedLicense.getLicenseConflict());
        detectedLicense.setLicenseConflict(licenseConflict);
        assertNotNull(detectedLicense.getLicenseConflict());
    }
}
