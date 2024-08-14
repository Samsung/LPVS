/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.report;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.service.LPVSLicenseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.thymeleaf.TemplateEngine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(ThymeleafAutoConfiguration.class)
@ContextConfiguration(classes = {LPVSReportBuilder.class})
@Slf4j
public class LPVSReportBuilderTest {

    @Autowired private TemplateEngine templateEngine;

    LPVSFile file1, file2, file3, file4;
    LPVSLicense lic1, lic2, lic3, lic4;
    LPVSLicenseService.Conflict<String, String> conflict1, conflict2;
    LPVSReportBuilder reportBuilder;

    @BeforeEach
    public void setUp() throws Exception {
        lic1 =
                new LPVSLicense() {
                    {
                        setLicenseName("MIT License");
                        setAccess("PERMITTED");
                        setSpdxId("MIT");
                    }
                };
        lic2 =
                new LPVSLicense() {
                    {
                        setLicenseName("GNU General Public License v3.0 only");
                        setAccess("PROHIBITED");
                        setSpdxId("GPL-3.0-only");
                    }
                };
        lic3 =
                new LPVSLicense() {
                    {
                        setLicenseName("GNU Lesser General Public License v2.0 or later");
                        setAccess("RESTRICTED");
                        setSpdxId("LGPL-2.0-or-later");
                    }
                };
        lic4 =
                new LPVSLicense() {
                    {
                        setLicenseName("Apache License 2.0");
                        setAccess("UNREVIEWED");
                        setSpdxId("Apache-2.0");
                    }
                };

        file1 = new LPVSFile();
        file1.setLicenses(
                new HashSet<>() {
                    {
                        add(lic1);
                    }
                });
        file1.setFilePath("local_file_path_1");
        file1.setComponentFilePath("component_file_path_1");
        file1.setComponentFileUrl("http://component_name_1/file_url");
        file1.setComponentName("component_name_1");
        file1.setComponentUrl("http://component_name_1/url");
        file1.setComponentVersion("v1.0.0");
        file1.setComponentVendor("component_vendor_1");
        file1.setSnippetMatch("80%");
        file1.setMatchedLines("5-17");

        file2 = new LPVSFile();
        file2.setLicenses(
                new HashSet<>() {
                    {
                        add(lic2);
                        add(lic3);
                    }
                });
        file2.setFilePath("local_file_path_2");
        file2.setComponentFilePath("component_file_path_2");
        file2.setComponentName("component_name_2");
        file2.setComponentUrl("http://component_name_2/url");
        file2.setComponentVersion("v2.0.0");
        file2.setComponentVendor("component_vendor_2");
        file2.setSnippetMatch("100%");
        file2.setMatchedLines("all");

        file3 = new LPVSFile();
        file3.setLicenses(
                new HashSet<>() {
                    {
                        add(lic4);
                    }
                });
        file3.setFilePath("local_file_path_3");
        file3.setComponentFilePath("component_file_path_3");
        file3.setComponentFileUrl("http://component_name_3/file_url");
        file3.setComponentName("component_name_3");
        file3.setComponentUrl("http://component_name_3/url");
        file3.setComponentVersion("v3.0.0");
        file3.setComponentVendor("component_vendor_3");
        file3.setSnippetMatch("20%");
        file3.setMatchedLines("1-10");

        file4 = new LPVSFile();
        file4.setLicenses(
                new HashSet<>() {
                    {
                        add(lic4);
                    }
                });
        file4.setFilePath("local_file_path_4");
        file4.setComponentFilePath("component_file_path_4");
        file4.setComponentName("component_name_4");
        file4.setComponentUrl("http://component_name_4/url");
        file4.setComponentVersion("v4.0.0");
        file4.setComponentVendor("component_vendor_4");
        file4.setSnippetMatch("50%");
        file4.setMatchedLines("5-10");

        conflict1 = new LPVSLicenseService.Conflict<>("GPL-3.0-only", "Apache-2.0");
        conflict2 = new LPVSLicenseService.Conflict<>("LGPL-2.0-or-later", "MIT");

        reportBuilder = new LPVSReportBuilder(templateEngine);
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    public void testGenerateHtmlReportSingleScan_Empty() {
        String actual = reportBuilder.generateHtmlReportSingleScan("some/path", null, null);
        assertThat(actual).contains(sdf.format(new Date())); // check title and scanDate
        assertThat(actual).contains("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithLicensesAndConflicts() {
        List<LPVSFile> scanResults = List.of(file1, file2, file3, file4);
        List<LPVSLicenseService.Conflict<String, String>> conflicts = List.of(conflict1, conflict2);
        String actual =
                reportBuilder.generateHtmlReportSingleScan("some/path", scanResults, conflicts);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).doesNotContain("No license problems detected");
        assertThat(actual).doesNotContain("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithLicenses() {
        List<LPVSFile> scanResults = List.of(file3, file4);
        String actual = reportBuilder.generateHtmlReportSingleScan("some/path", scanResults, null);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).doesNotContain("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }

    @Test
    public void testGenerateHtmlReportSingleScan_WithPermittedLicenses() {
        List<LPVSFile> scanResults = List.of(file1);
        String actual = reportBuilder.generateHtmlReportSingleScan("some/path", scanResults, null);
        assertThat(actual).contains(sdf.format(new Date()));
        assertThat(actual).contains("No license problems detected");
        assertThat(actual).contains("No license conflicts detected");
    }
}
