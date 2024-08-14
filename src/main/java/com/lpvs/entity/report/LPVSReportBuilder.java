/**
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.report;

import com.lpvs.LicensePreValidationService;
import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.service.LPVSLicenseService;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class responsible for building reports based on the results of license scanning.
 */
@Component
public class LPVSReportBuilder {

    /**
     * Creates a new instance of the LPVSReportBuilder class.
     *
     * @param templateEngine the template engine to use for generating reports
     */
    @Autowired
    public LPVSReportBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * The template engine to use for generating reports.
     */
    private final TemplateEngine templateEngine;

    /**
     * The type of license detection scanner.
     */
    @Value("${scanner:scanoss}")
    private String scannerType;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Map<String, GroupInfo<Map<String, GroupInfo<Map<String, GroupInfo<List<LPVSFile>>>>>>>
            detectedLicenseInfo = null;

    private final String permitted = "PERMITTED";
    private final String restricted = "RESTRICTED";
    private final String prohibited = "PROHIBITED";
    private final String unreviewed = "UNREVIEWED";

    /**
     * Generates an HTML report for a single scan.
     *
     * @param path the path to the source folder for scan or pull request URL
     * @param scanResults the results of the license scan
     * @param conflicts a list of license conflicts found during the scan
     * @return the HTML code of the generated report
     */
    public String generateHtmlReportSingleScan(
            String path,
            List<LPVSFile> scanResults,
            List<LPVSLicenseService.Conflict<String, String>> conflicts) {
        Context context = new Context();
        String date = sdf.format(new Date());

        context.setVariable("title", "Report-LPVS-" + date);
        context.setVariable("scanDate", date);
        context.setVariable("codeLocation", path);
        context.setVariable("usedScanner", scannerType);
        context.setVariable(
                "lpvsVersion", LicensePreValidationService.getVersion(new MavenXpp3Reader()));

        groupScanResultsForLicenseTable(scanResults);
        long prohibitedLicenses = getDetectedLicenseCountByType(prohibited);
        long restrictedLicenses = getDetectedLicenseCountByType(restricted);
        long unreviewedLicenses = getDetectedLicenseCountByType(unreviewed);
        long licenseDetected = prohibitedLicenses + restrictedLicenses + unreviewedLicenses;

        context.setVariable("licenseDetected", licenseDetected);
        context.setVariable("prohibitedLicenses", prohibitedLicenses);
        context.setVariable("restrictedLicenses", restrictedLicenses);
        context.setVariable("unreviewedLicenses", unreviewedLicenses);

        if (scanResults != null && !scanResults.isEmpty()) {
            context.setVariable("licenseTable", generateLicenseTable());
        } else {
            context.setVariable("licenseTable", null);
        }

        if (conflicts != null && !conflicts.isEmpty()) {
            context.setVariable("licenseConflicts", conflicts.size());
            context.setVariable("conflictTable", generateLicenseConflictsTable(conflicts));
        } else {
            context.setVariable("licenseConflicts", 0);
            context.setVariable("conflictTable", null);
        }

        return templateEngine.process("report_single_scan", context);
    }

    /**
     * A class representing a group of elements with a count.
     *
     * @param <T> the type of elements in the group
     */
    @AllArgsConstructor
    private static class GroupInfo<T> {
        /**
         * The number of elements in the group.
         */
        @Getter private long count;

        /**
         * The elements in the group.
         */
        private T elements;
    }

    /**
     * Function that returns the number of licenses detected for the given type.
     *
     * @param type the license type to count
     * @return the number of licenses detected for the given type
     */
    private long getDetectedLicenseCountByType(String type) {
        if (detectedLicenseInfo == null || detectedLicenseInfo.get(type) == null) {
            return 0;
        }
        return detectedLicenseInfo.get(type).elements.size();
    }

    /**
     * Groups the scan results by license type for display in the license table.
     *
     * @param scanResults the results of the license scan
     */
    private void groupScanResultsForLicenseTable(List<LPVSFile> scanResults) {
        if (detectedLicenseInfo == null && scanResults != null && !scanResults.isEmpty()) {
            List<LPVSFile> filesScanResults = getLpvsFilesFromScanResults(scanResults);

            detectedLicenseInfo =
                    filesScanResults.stream()
                            .collect(
                                    Collectors.groupingBy(
                                            LPVSFile ->
                                                    LPVSFile.getLicenses().stream()
                                                            .findFirst()
                                                            .get()
                                                            .getAccess(),
                                            Collectors.collectingAndThen(
                                                    Collectors.groupingBy(
                                                            LPVSFile ->
                                                                    LPVSFile.getLicenses().stream()
                                                                            .findFirst()
                                                                            .get()
                                                                            .getSpdxId(),
                                                            Collectors.collectingAndThen(
                                                                    Collectors.groupingBy(
                                                                            LPVSFile ->
                                                                                    LPVSFile
                                                                                                    .getComponentVendor()
                                                                                            + " / "
                                                                                            + LPVSFile
                                                                                                    .getComponentName()
                                                                                            + ":::"
                                                                                            + LPVSFile
                                                                                                    .getComponentUrl(),
                                                                            Collectors
                                                                                    .collectingAndThen(
                                                                                            Collectors
                                                                                                    .toList(),
                                                                                            elements ->
                                                                                                    new GroupInfo<>(
                                                                                                            elements
                                                                                                                    .size(),
                                                                                                            elements))),
                                                                    groupedByComponent ->
                                                                            new GroupInfo<>(
                                                                                    groupedByComponent
                                                                                            .values()
                                                                                            .stream()
                                                                                            .mapToLong(
                                                                                                    GroupInfo
                                                                                                            ::getCount)
                                                                                            .sum(),
                                                                                    groupedByComponent))),
                                                    groupedByLicense ->
                                                            new GroupInfo<>(
                                                                    groupedByLicense
                                                                            .values()
                                                                            .stream()
                                                                            .mapToLong(
                                                                                    GroupInfo
                                                                                            ::getCount)
                                                                            .sum(),
                                                                    groupedByLicense))));
        }
    }

    /**
     * Function that returns the converted list of LPVS files with a single license.
     *
     * @param scanResults the results of the license scan
     * @return a list of LPVS files with a single license
     */
    private List<LPVSFile> getLpvsFilesFromScanResults(List<LPVSFile> scanResults) {
        List<LPVSFile> filesScanResults = new ArrayList<>();
        for (LPVSFile file : scanResults) {
            Set<LPVSLicense> licenses = file.getLicenses();
            for (LPVSLicense license : licenses) {
                LPVSFile file_ =
                        new LPVSFile() {
                            {
                                setFilePath(file.getFilePath());
                                setAbsoluteFilePath(file.getAbsoluteFilePath());
                                setSnippetType(file.getSnippetType());
                                setSnippetMatch(file.getSnippetMatch());
                                setMatchedLines(file.getMatchedLines());
                                setLicenses(new HashSet<>(Collections.singletonList(license)));
                                setComponentFilePath(file.getComponentFilePath());
                                setComponentFileUrl(file.getComponentFileUrl());
                                setComponentName(file.getComponentName());
                                setComponentLines(file.getComponentLines());
                                setComponentUrl(file.getComponentUrl());
                                setComponentVendor(file.getComponentVendor());
                                setComponentVersion(file.getComponentVersion());
                            }
                        };
                filesScanResults.add(file_);
            }
        }
        return filesScanResults;
    }

    /**
     * Generates the license table HTML content.
     *
     * @return the HTML content for the license table
     */
    private String generateLicenseTable() {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<table>");
        htmlBuilder
                .append("<tr>")
                .append("<th>License Type / Explanation</th>")
                .append("<th>License SPDX ID</th>")
                .append("<th>Vendor / Component</th>")
                .append("<th>Version</th>")
                .append("<th>Repository File Path</th>")
                .append("<th>Component File Path</th>")
                .append("<th>Matched Lines</th>")
                .append("<th>Match Value</th>")
                .append("<tr>");
        // Prohibited licenses
        addBlockOfTableForLicenseType(htmlBuilder, prohibited);
        // Restricted licenses
        addBlockOfTableForLicenseType(htmlBuilder, restricted);
        // Unreviewed licenses
        addBlockOfTableForLicenseType(htmlBuilder, unreviewed);
        // Permitted licenses
        addBlockOfTableForLicenseType(htmlBuilder, permitted);

        htmlBuilder.append("</table>");
        return htmlBuilder.toString();
    }

    /**
     * Adds a block of HTML content for a specific license type to the license table.
     *
     * @param htmlBuilder the StringBuilder object to which the HTML content will be appended
     * @param type the license type for which to add the block of HTML content
     */
    private void addBlockOfTableForLicenseType(StringBuilder htmlBuilder, String type) {
        long detectedLicenseCountByType = getDetectedLicenseCountByType(type);
        boolean isNewRow;
        if (detectedLicenseCountByType > 0) {
            htmlBuilder.append("<tr>");
            isNewRow = true;

            htmlBuilder
                    .append("<td rowspan=\"")
                    .append(detectedLicenseInfo.get(type).getCount())
                    .append("\">");
            switch (type.toUpperCase()) {
                case prohibited:
                    htmlBuilder
                            .append("<span style=\"color: red; font-weight: bold;\">")
                            .append(type)
                            .append("</span>");
                    break;
                case restricted:
                case unreviewed:
                    htmlBuilder
                            .append("<span style=\"color: orange; font-weight: bold;\">")
                            .append(type)
                            .append("</span>");
                    break;
                case permitted:
                    htmlBuilder
                            .append("<span style=\"color: green; font-weight: bold;\">")
                            .append(type)
                            .append("</span>");
                    break;
                default:
                    throw new IllegalStateException(
                            "Unexpected value for the license type: " + type);
            }
            htmlBuilder.append(" / ");
            htmlBuilder.append(getExplanationForLicenseType(type));
            htmlBuilder.append("</td>");

            // license spdx
            for (String licenseSpdxs : detectedLicenseInfo.get(type).elements.keySet()) {
                if (!isNewRow) {
                    htmlBuilder.append("<tr>");
                    isNewRow = true;
                }
                htmlBuilder
                        .append("<td rowspan=\"")
                        .append(detectedLicenseInfo.get(type).elements.get(licenseSpdxs).getCount())
                        .append("\">");
                htmlBuilder.append(licenseSpdxs);
                htmlBuilder.append("</td>");

                // vendor + component
                for (String componentInfo :
                        detectedLicenseInfo
                                .get(type)
                                .elements
                                .get(licenseSpdxs)
                                .elements
                                .keySet()) {
                    if (!isNewRow) {
                        htmlBuilder.append("<tr>");
                        isNewRow = true;
                    }

                    htmlBuilder
                            .append("<td rowspan=\"")
                            .append(
                                    detectedLicenseInfo
                                            .get(type)
                                            .elements
                                            .get(licenseSpdxs)
                                            .elements
                                            .get(componentInfo)
                                            .getCount())
                            .append("\">");
                    htmlBuilder
                            .append("<a href=\"")
                            .append(componentInfo.split(":::")[1])
                            .append("\">")
                            .append(componentInfo.split(":::")[0])
                            .append("</a>");
                    htmlBuilder.append("</td>");

                    // file info
                    for (LPVSFile fileInfo :
                            detectedLicenseInfo
                                    .get(type)
                                    .elements
                                    .get(licenseSpdxs)
                                    .elements
                                    .get(componentInfo)
                                    .elements) {
                        if (!isNewRow) {
                            htmlBuilder.append("<tr>");
                        }
                        htmlBuilder
                                .append("<td>")
                                .append(fileInfo.getComponentVersion())
                                .append("</td><td>")
                                .append(fileInfo.getFilePath())
                                .append("</td><td>");

                        if (!StringUtils.isBlank(fileInfo.getComponentFileUrl())) {
                            htmlBuilder
                                    .append("<a href=\"")
                                    .append(fileInfo.getComponentFileUrl())
                                    .append("\">")
                                    .append(fileInfo.getComponentFilePath())
                                    .append("</a>");
                        } else {
                            htmlBuilder.append(fileInfo.getComponentFilePath());
                        }

                        htmlBuilder
                                .append("</td><td>")
                                .append(fileInfo.getMatchedLines())
                                .append("</td><td>")
                                .append(fileInfo.getSnippetMatch())
                                .append("</td>");

                        htmlBuilder.append("</tr>");
                        isNewRow = false;
                    }
                }
            }
        }
    }

    /**
     * Generates the license conflicts table HTML content.
     *
     * @param conflicts a list of license conflicts
     * @return the HTML content for the license conflicts table
     */
    private String generateLicenseConflictsTable(
            List<LPVSLicenseService.Conflict<String, String>> conflicts) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<table>");
        htmlBuilder
                .append("<tr>")
                .append("<th>Conflict</th>")
                .append("<th>Explanation</th>")
                .append("<tr>");

        for (LPVSLicenseService.Conflict<String, String> conflict : conflicts) {
            htmlBuilder
                    .append("<tr>")
                    .append("<td>")
                    .append(conflict.l1)
                    .append(" and ")
                    .append(conflict.l2)
                    .append("</td>")
                    .append("<td>")
                    .append(getExplanationForLicenseConflict(conflict.l1, conflict.l2))
                    .append("</td>")
                    .append("</tr>");
        }
        htmlBuilder.append("</table>");
        return htmlBuilder.toString();
    }

    /**
     * Retrieves the explanation for a specific license type.
     *
     * @param type the license type for which to retrieve the explanation
     * @return the explanation for the specified license type
     */
    private String getExplanationForLicenseType(String type) {
        switch (type.toUpperCase()) {
            case prohibited:
                return "This license prohibits the use of the licensed code in certain contexts, such as commercial software development.";
            case restricted:
                return "This license required compliance with specific obligations. It is crucial to carefully review and adhere to these obligations before using the licensed code.";
            case unreviewed:
                return "This license has not been reviewed thoroughly and may contain unknown risks or limitations. It is recommended to review these licenses carefully before using the licensed code.";
            case permitted:
                return "This license permits free usage, modification, and distribution of the licensed code without any restrictions.";
            default:
                throw new IllegalStateException("Unexpected value for the license type: " + type);
        }
    }

    /**
     * Retrieves the explanation for a specific license conflict.
     *
     * @param lic1 the first license involved in the conflict
     * @param lic2 the second license involved in the conflict
     * @return the explanation for the specified license conflict
     */
    private String getExplanationForLicenseConflict(String lic1, String lic2) {
        return "These two licenses are incompatible due to their conflicting terms and conditions. It is recommended to resolve this conflict by choosing either "
                + lic1
                + " or "
                + lic2
                + " for the affected components.";
    }
}
