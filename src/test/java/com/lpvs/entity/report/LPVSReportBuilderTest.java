/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.report;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSVcs;
import com.lpvs.service.LPVSLicenseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.lpvs.entity.report.LPVSReportBuilder.saveHTMLToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
@ContextConfiguration(classes = {LPVSReportBuilder.class})
@Slf4j
public class LPVSReportBuilderTest {

    @Autowired private TemplateEngine templateEngine;

    LPVSFile fileLicPermitted,
            fileLicProhibitedRestricted,
            fileLicUnreviewed_1,
            fileLicUnreviewed_2,
            fileLicUnreviewed_3,
            fileLicIncorrect;
    LPVSLicense licPermitted,
            licProhibited,
            licRestricted,
            licUnreviewed,
            licUnreviewed_2,
            licIncorrect;
    LPVSLicenseService.Conflict<String, String> conflict1, conflict2;
    LPVSReportBuilder reportBuilder;

    @TempDir Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        licPermitted =
                new LPVSLicense() {
                    {
                        setLicenseName("MIT License");
                        setAccess("PERMITTED");
                        setSpdxId("MIT");
                    }
                };
        licProhibited =
                new LPVSLicense() {
                    {
                        setLicenseName("GNU General Public License v3.0 only");
                        setAccess("PROHIBITED");
                        setSpdxId("GPL-3.0-only");
                    }
                };
        licRestricted =
                new LPVSLicense() {
                    {
                        setLicenseName("GNU Lesser General Public License v2.0 or later");
                        setAccess("RESTRICTED");
                        setSpdxId("LGPL-2.0-or-later");
                    }
                };
        licUnreviewed =
                new LPVSLicense() {
                    {
                        setLicenseName("Apache License 2.0");
                        setAccess("UNREVIEWED");
                        setSpdxId("Apache-2.0");
                    }
                };

        licUnreviewed_2 =
                new LPVSLicense() {
                    {
                        setLicenseName("BSD 3-Clause \"New\" or \"Revised\" License");
                        setAccess("UNREVIEWED");
                        setSpdxId("BSD-3-Clause");
                    }
                };

        licIncorrect =
                new LPVSLicense() {
                    {
                        setLicenseName("Some incorrect license");
                        setAccess("INCORRECT");
                        setSpdxId("Some-license");
                    }
                };

        fileLicPermitted = new LPVSFile();
        fileLicPermitted.setLicenses(
                new HashSet<>() {
                    {
                        add(licPermitted);
                    }
                });
        fileLicPermitted.setFilePath("local_file_path_1");
        fileLicPermitted.setComponentFilePath("component_file_path_1");
        fileLicPermitted.setComponentFileUrl("http://component_name_1/file_url");
        fileLicPermitted.setComponentName("component_name_1");
        fileLicPermitted.setComponentUrl("http://component_name_1/url");
        fileLicPermitted.setComponentVersion("v1.0.0");
        fileLicPermitted.setComponentVendor("component_vendor_1");
        fileLicPermitted.setSnippetMatch("80%");
        fileLicPermitted.setMatchedLines("5-17");

        fileLicProhibitedRestricted = new LPVSFile();
        fileLicProhibitedRestricted.setLicenses(
                new HashSet<>() {
                    {
                        add(licProhibited);
                        add(licRestricted);
                    }
                });
        fileLicProhibitedRestricted.setFilePath("local_file_path_2");
        fileLicProhibitedRestricted.setComponentFilePath("component_file_path_2");
        fileLicProhibitedRestricted.setComponentName("component_name_2");
        fileLicProhibitedRestricted.setComponentUrl("http://component_name_2/url");
        fileLicProhibitedRestricted.setComponentVersion("v2.0.0");
        fileLicProhibitedRestricted.setComponentVendor("component_vendor_2");
        fileLicProhibitedRestricted.setSnippetMatch("100%");
        fileLicProhibitedRestricted.setMatchedLines("all");

        fileLicUnreviewed_1 = new LPVSFile();
        fileLicUnreviewed_1.setLicenses(
                new HashSet<>() {
                    {
                        add(licUnreviewed_2);
                    }
                });
        fileLicUnreviewed_1.setFilePath("local_file_path_3");
        fileLicUnreviewed_1.setComponentFilePath("component_file_path_3");
        fileLicUnreviewed_1.setComponentFileUrl("http://component_name_3/file_url");
        fileLicUnreviewed_1.setComponentName("component_name_3");
        fileLicUnreviewed_1.setComponentUrl("http://component_name_3/url");
        fileLicUnreviewed_1.setComponentVersion("v3.0.0");
        fileLicUnreviewed_1.setComponentVendor("component_vendor_3");
        fileLicUnreviewed_1.setSnippetMatch("20%");
        fileLicUnreviewed_1.setMatchedLines("1-10");

        fileLicUnreviewed_2 = new LPVSFile();
        fileLicUnreviewed_2.setLicenses(
                new HashSet<>() {
                    {
                        add(licUnreviewed);
                    }
                });
        fileLicUnreviewed_2.setFilePath("local_file_path_4");
        fileLicUnreviewed_2.setComponentFilePath("component_file_path_4");
        fileLicUnreviewed_2.setComponentName("component_name_4");
        fileLicUnreviewed_2.setComponentUrl("http://component_name_4/url");
        fileLicUnreviewed_2.setComponentVersion("v4.0.0");
        fileLicUnreviewed_2.setComponentVendor("component_vendor_4");
        fileLicUnreviewed_2.setSnippetMatch("50%");
        fileLicUnreviewed_2.setMatchedLines("5-10");

        fileLicUnreviewed_3 = new LPVSFile();
        fileLicUnreviewed_3.setLicenses(
                new HashSet<>() {
                    {
                        add(licUnreviewed);
                    }
                });
        fileLicUnreviewed_3.setFilePath("local_file_path_4");
        fileLicUnreviewed_3.setComponentFilePath("component_file_path_4");
        fileLicUnreviewed_3.setComponentName("component_name_4");
        fileLicUnreviewed_3.setComponentUrl("http://component_name_4/url");
        fileLicUnreviewed_3.setComponentVersion("v4.0.0");
        fileLicUnreviewed_3.setComponentVendor("component_vendor_4");
        fileLicUnreviewed_3.setSnippetMatch("40%");
        fileLicUnreviewed_3.setMatchedLines("1-10");

        fileLicIncorrect = new LPVSFile();
        fileLicIncorrect.setLicenses(
                new HashSet<>() {
                    {
                        add(licIncorrect);
                    }
                });
        fileLicIncorrect.setFilePath("local_file_path_4");
        fileLicIncorrect.setComponentFilePath("component_file_path_4");
        fileLicIncorrect.setComponentName("component_name_4");
        fileLicIncorrect.setComponentUrl("http://component_name_4/url");
        fileLicIncorrect.setComponentVersion("v4.0.0");
        fileLicIncorrect.setComponentVendor("component_vendor_4");
        fileLicIncorrect.setSnippetMatch("50%");
        fileLicIncorrect.setMatchedLines("5-10");

        conflict1 = new LPVSLicenseService.Conflict<>("GPL-3.0-only", "Apache-2.0");
        conflict2 = new LPVSLicenseService.Conflict<>("LGPL-2.0-or-later", "MIT");

        reportBuilder = new LPVSReportBuilder(templateEngine);
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    public void testGenerateHtmlReportSingleScan_Empty() {
        String actual =
                reportBuilder.generateHtmlReportSingleScan(
                        "some/path", new ArrayList<>(), null, null, null);
        assertThat(actual).contains(sdf.format(new Date())); // check title and scanDate
        assertThat(actual).contains("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithLicensesAndConflicts() {
        List<LPVSFile> scanResults =
                List.of(
                        fileLicPermitted,
                        fileLicProhibitedRestricted,
                        fileLicUnreviewed_1,
                        fileLicUnreviewed_2);
        List<LPVSLicenseService.Conflict<String, String>> conflicts = List.of(conflict1, conflict2);
        String actual =
                reportBuilder.generateHtmlReportSingleScan(
                        "some/path", scanResults, conflicts, null, null);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).doesNotContain("No license problems detected");
        assertThat(actual).doesNotContain("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithLicenses() {
        List<LPVSFile> scanResults = List.of(fileLicUnreviewed_1, fileLicUnreviewed_2);
        String actual =
                reportBuilder.generateHtmlReportSingleScan(
                        "some/path", scanResults, null, null, null);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).doesNotContain("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithPermittedLicenses() {
        List<LPVSFile> scanResults = List.of(fileLicPermitted);
        String actual =
                reportBuilder.generateHtmlReportSingleScan(
                        "some/path", scanResults, null, null, null);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).contains("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithSimilarLicenses() {
        List<LPVSFile> scanResults =
                List.of(fileLicUnreviewed_1, fileLicUnreviewed_2, fileLicUnreviewed_3);
        String actual =
                reportBuilder.generateHtmlReportSingleScan(
                        "some/path", scanResults, new ArrayList<>(), null, null);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).doesNotContain("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }

    @Test
    void testSaveHTMLToFile() throws IOException {
        String htmlContent = "<html><body><p>Test HTML</p></body></html>";
        String filePath = "test-output.html";

        saveHTMLToFile(htmlContent, filePath);

        Path path = Paths.get(filePath);
        assertTrue(Files.exists(path));
        String fileContent = Files.readString(path);
        assertEquals(htmlContent, fileContent);

        // Clean up: delete the created file
        Files.deleteIfExists(path);
    }

    @Test
    void saveHTMLToFile_CatchBlock_N() {
        String htmlContent = "<html><body></body></html>";
        Path invalidPath = tempDir.resolve("invalid/path/with/special/characters");
        saveHTMLToFile(htmlContent, invalidPath.toString());
        assertFalse(Files.exists(invalidPath));
    }

    private LPVSFile createSampleFile(String filePath, String matchedLines, String baseAccess) {
        final Long baseLicenseId = 1234567890L;
        final String baseLicenseName = "licenseName";
        final String baseSpdxId = "spdxId";
        final String baseAlternativeName = "licenseNameAlternative";

        LPVSLicense lpvsLicense =
                new LPVSLicense(
                        baseLicenseId,
                        baseLicenseName,
                        baseSpdxId,
                        baseAccess,
                        baseAlternativeName,
                        null);

        Set<LPVSLicense> licenses = new HashSet<>(List.of(lpvsLicense));
        LPVSFile file = new LPVSFile();
        file.setFilePath(filePath);
        file.setMatchedLines(matchedLines);
        file.setLicenses(licenses);
        return file;
    }

    @Test
    void testReportCommentBuilder_LicenseDetectedNoConflicts() {
        LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
        List<LPVSFile> scanResults = new ArrayList<>();
        List<LPVSLicenseService.Conflict<String, String>> conflicts = new ArrayList<>();

        scanResults.add(createSampleFile("testPath1", "test1", "UNREVIEWED"));
        scanResults.add(createSampleFile("testPath1", "test1", "PROHIBITED"));
        scanResults.add(createSampleFile("testPath1", "test1", "RESTRICTED"));
        scanResults.add(createSampleFile("testPath1", "test1", "PERMITTED"));
        String comment =
                reportBuilder.generateCommandLineComment("testPath1", scanResults, conflicts);
        assertNotNull(comment);
    }

    @Test
    void testReportCommentBuilder_LicenseDetectedConflictsDetected() {
        LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
        List<LPVSFile> scanResults = new ArrayList<>();
        scanResults.add(createSampleFile("testPath1", "test1", "PROHIBITED"));
        LPVSLicenseService.Conflict<String, String> conflict_1 =
                new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");
        List<LPVSLicenseService.Conflict<String, String>> conflicts =
                List.of(conflict_1, conflict_1);
        String comment =
                reportBuilder.generateCommandLineComment("testPath1", scanResults, conflicts);
        assertNotNull(comment);
    }

    @Test
    void testReportCommentBuilder_NoLicenseDetectedNoConflicts() {
        LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
        String comment = reportBuilder.generateCommandLineComment("testPath1", null, null);
        assertNotNull(comment);
    }

    @Test
    void testGeneratePullRequestComment_LicenseDetectedNoConflicts() {
        LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
        List<LPVSFile> scanResults = new ArrayList<>();
        List<LPVSLicenseService.Conflict<String, String>> conflicts = new ArrayList<>();

        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/1");
        webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

        scanResults.add(createSampleFile("testPath1", "test1", "UNREVIEWED"));
        scanResults.add(createSampleFile("testPath1", "test1", "PROHIBITED"));
        scanResults.add(createSampleFile("testPath1", "test1", "RESTRICTED"));
        scanResults.add(createSampleFile("testPath1", "test1", "PERMITTED"));

        String comment =
                reportBuilder.generatePullRequestComment(
                        scanResults, conflicts, webhookConfig, LPVSVcs.GITHUB);
        assertNotNull(comment);

        comment =
                reportBuilder.generatePullRequestComment(
                        scanResults, conflicts, webhookConfig, LPVSVcs.GERRIT);
        assertNotNull(comment);
    }

    @Test
    void testGeneratePullRequestComment_LicenseDetectedConflictsDetected() {
        LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
        List<LPVSFile> scanResults = new ArrayList<>();
        scanResults.add(createSampleFile("testPath1", "test1", "PROHIBITED"));
        LPVSLicenseService.Conflict<String, String> conflict_1 =
                new LPVSLicenseService.Conflict<>("MIT", "Apache-2.0");
        List<LPVSLicenseService.Conflict<String, String>> conflicts =
                List.of(conflict_1, conflict_1);
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/1");
        webhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");

        String comment =
                reportBuilder.generatePullRequestComment(
                        scanResults, conflicts, webhookConfig, LPVSVcs.GITHUB);
        assertNotNull(comment);

        comment =
                reportBuilder.generatePullRequestComment(
                        scanResults, conflicts, webhookConfig, LPVSVcs.GERRIT);
        assertNotNull(comment);
    }

    @Test
    void testGeneratePullRequestComment_NoLicenseDetectedNoConflicts() {
        LPVSReportBuilder reportBuilder = new LPVSReportBuilder(null);
        String comment = reportBuilder.generatePullRequestComment(null, null, null, LPVSVcs.GITHUB);
        assertNotNull(comment);

        comment = reportBuilder.generatePullRequestComment(null, null, null, LPVSVcs.GERRIT);
        assertNotNull(comment);
    }
}
