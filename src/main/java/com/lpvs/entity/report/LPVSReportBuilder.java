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
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.lpvs.util.LPVSCommentUtil.getMatchedLinesAsLink;

/**
 * A class responsible for building reports based on the results of license scanning.
 */
@Component
@Slf4j
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

    /**
     * The version of LPVS application.
     */
    @Value("${lpvs.version:Unknown}")
    private String lpvsVersion;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String permitted = "PERMITTED";
    private final String restricted = "RESTRICTED";
    private final String prohibited = "PROHIBITED";
    private final String unreviewed = "UNREVIEWED";
    private final String boldStart = "\033[1m";
    private final String boldEnd = "\033[0m";

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
     * Generates an HTML report for a single scan.
     *
     * @param path the path to the source folder for scan or pull request URL
     * @param scanResults the results of the license scan
     * @param conflicts a list of license conflicts found during the scan
     * @param webhookConfig configuration related to the repository and webhook
     * @param vcs the string representation of the version control system
     * @return the HTML code of the generated report
     */
    public String generateHtmlReportSingleScan(
            String path,
            List<LPVSFile> scanResults,
            List<LPVSLicenseService.Conflict<String, String>> conflicts,
            LPVSQueue webhookConfig,
            LPVSVcs vcs) {
        Context context = new Context();
        String date = sdf.format(new Date());

        context.setVariable("title", "Report-LPVS-" + date);
        context.setVariable("scanDate", date);
        context.setVariable("codeLocation", path);
        context.setVariable("usedScanner", scannerType);
        context.setVariable("lpvsVersion", lpvsVersion);

        Map<String, GroupInfo<?>> detectedLicenseInfo =
                groupScanResultsForLicenseTable(scanResults);

        long prohibitedLicenses = getDetectedLicenseCountByType(detectedLicenseInfo, prohibited);
        long restrictedLicenses = getDetectedLicenseCountByType(detectedLicenseInfo, restricted);
        long unreviewedLicenses = getDetectedLicenseCountByType(detectedLicenseInfo, unreviewed);
        long licenseDetected = prohibitedLicenses + restrictedLicenses + unreviewedLicenses;

        context.setVariable("licenseDetected", licenseDetected);
        context.setVariable("prohibitedLicenses", prohibitedLicenses);
        context.setVariable("restrictedLicenses", restrictedLicenses);
        context.setVariable("unreviewedLicenses", unreviewedLicenses);

        if (scanResults != null && !scanResults.isEmpty()) {
            context.setVariable(
                    "licenseTable",
                    generateLicenseTableHTML(detectedLicenseInfo, webhookConfig, vcs));
        } else {
            context.setVariable("licenseTable", null);
        }

        if (conflicts != null && !conflicts.isEmpty()) {
            context.setVariable("licenseConflicts", conflicts.size());
            context.setVariable("conflictTable", generateLicenseConflictsTableHTML(conflicts));
        } else {
            context.setVariable("licenseConflicts", 0);
            context.setVariable("conflictTable", null);
        }

        return templateEngine.process("report_single_scan", context);
    }

    /**
     * Generates a formatted string for an LPVS command line comment.
     *
     * @param path The path to the source folder for scan or pull request URL
     * @param scanResults the results of the license scan
     * @param conflicts a list of license conflicts found during the scan
     * @return A string containing scan results in command line friendly format.
     */
    public String generateCommandLineComment(
            String path,
            List<LPVSFile> scanResults,
            List<LPVSLicenseService.Conflict<String, String>> conflicts) {
        StringBuilder commentBuilder = new StringBuilder();
        String date = sdf.format(new Date());
        commentBuilder.append("\n");
        commentBuilder.append(boldStart + "Scan date: " + boldEnd + date + "\n");
        commentBuilder.append(boldStart + "Source code location: " + boldEnd + path + "\n");
        commentBuilder.append(boldStart + "Used scanner: " + boldEnd + scannerType + "\n");
        commentBuilder.append(boldStart + "Version of LPVS: " + boldEnd + lpvsVersion + "\n\n");

        commentBuilder.append(boldStart + "Detected Licenses" + boldEnd + "\n\n");

        Map<String, GroupInfo<?>> detectedLicenseInfo =
                groupScanResultsForLicenseTable(scanResults);
        long prohibitedLicenses = getDetectedLicenseCountByType(detectedLicenseInfo, prohibited);
        long restrictedLicenses = getDetectedLicenseCountByType(detectedLicenseInfo, restricted);
        long unreviewedLicenses = getDetectedLicenseCountByType(detectedLicenseInfo, unreviewed);
        long licenseDetected = prohibitedLicenses + restrictedLicenses + unreviewedLicenses;

        if (licenseDetected > 0) {
            commentBuilder.append(
                    "Potential license problem(s) detected: " + licenseDetected + "\n");
            if (prohibitedLicenses > 0) {
                commentBuilder.append(" - Prohibited license(s): " + prohibitedLicenses + "\n");
            }
            if (restrictedLicenses > 0) {
                commentBuilder.append(" - Restricted license(s): " + restrictedLicenses + "\n");
            }
            if (unreviewedLicenses > 0) {
                commentBuilder.append(" - Unreviewed license(s): " + unreviewedLicenses + "\n");
            }
        } else {
            commentBuilder.append("No license problems detected.\n");
        }

        if (scanResults != null && !scanResults.isEmpty()) {
            commentBuilder.append("\n");
            for (LPVSFile file : scanResults) {
                commentBuilder.append(boldStart + "File: " + boldEnd);
                commentBuilder.append(file.getFilePath());
                commentBuilder.append("\n");
                commentBuilder.append(boldStart + "License(s): " + boldEnd);
                commentBuilder.append(file.convertLicensesToString(null));
                commentBuilder.append(boldStart + "Component: " + boldEnd);
                commentBuilder.append(file.getComponentName());
                commentBuilder.append(" (");
                commentBuilder.append(file.getComponentFilePath());
                commentBuilder.append(")\n");
                commentBuilder.append(boldStart + "Matched Lines: " + boldEnd);
                commentBuilder.append(file.getMatchedLines());
                commentBuilder.append("\n");
                commentBuilder.append(boldStart + "Snippet Match: " + boldEnd);
                commentBuilder.append(file.getSnippetMatch());
                commentBuilder.append("\n\n");
            }
        }

        commentBuilder.append("\n");
        commentBuilder.append(boldStart + "Detected License Conflicts" + boldEnd + "\n\n");
        if (conflicts != null && !conflicts.isEmpty()) {
            commentBuilder.append(
                    "Potential license conflict(s) detected: " + conflicts.size() + "\n");
            for (LPVSLicenseService.Conflict<String, String> conflict : conflicts) {
                commentBuilder.append(" - " + conflict.l1 + " and " + conflict.l2 + "\n");
            }
        } else {
            commentBuilder.append("No license conflicts detected.\n");
        }

        return commentBuilder.toString();
    }

    /**
     * Saves HTML report to given location.
     *
     * @param htmlContent   The string, containing report in HTML format.
     * @param filePath      The path to expected html report file.
     */
    public static void saveHTMLToFile(String htmlContent, String filePath) {
        File file = new File(filePath);
        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(htmlContent);
            log.info("LPVS report saved to: " + filePath);
        } catch (IOException ex) {
            log.error("Error during saving HTML report: " + ex.getMessage());
        }
    }

    /**
     * Generates the license conflicts table HTML content.
     *
     * @param conflicts a list of license conflicts
     * @return the HTML content for the license conflicts table
     */
    private String generateLicenseConflictsTableHTML(
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
     * Retrieves the explanation for a specific license conflict.
     *
     * @param lic1 the first license involved in the conflict
     * @param lic2 the second license involved in the conflict
     * @return the explanation for the specified license conflict
     */
    private String getExplanationForLicenseConflict(String lic1, String lic2) {
        return "These two licenses are incompatible due to their conflicting terms and conditions. "
                + "It is recommended to resolve this conflict by choosing either "
                + lic1
                + " or "
                + lic2
                + " for the affected components.";
    }

    /**
     * Generates the license table HTML content.
     *
     * @param detectedLicenseInfo grouped scan results by license SPDX ID and access type, component name and vendor
     * @param webhookConfig configuration related to the repository and webhook
     * @param vcs the string representation of the version control system
     * @return the HTML content for the license table
     */
    private String generateLicenseTableHTML(
            Map<String, GroupInfo<?>> detectedLicenseInfo, LPVSQueue webhookConfig, LPVSVcs vcs) {
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
        addBlockOfTableForLicenseTypeHTML(
                htmlBuilder, detectedLicenseInfo, prohibited, webhookConfig, vcs);
        // Restricted licenses
        addBlockOfTableForLicenseTypeHTML(
                htmlBuilder, detectedLicenseInfo, restricted, webhookConfig, vcs);
        // Unreviewed licenses
        addBlockOfTableForLicenseTypeHTML(
                htmlBuilder, detectedLicenseInfo, unreviewed, webhookConfig, vcs);
        // Permitted licenses
        addBlockOfTableForLicenseTypeHTML(
                htmlBuilder, detectedLicenseInfo, permitted, webhookConfig, vcs);

        htmlBuilder.append("</table>");
        return htmlBuilder.toString();
    }

    /**
     * Adds a block of HTML content for a specific license type to the license table.
     *
     * @param htmlBuilder the StringBuilder object to which the HTML content will be appended
     * @param detectedLicenseInfo grouped scan results by license SPDX ID and access type, component name and vendor
     * @param type the license type for which to add the block of HTML content
     * @param webhookConfig configuration related to the repository and webhook
     * @param vcs the string representation of the version control system
     */
    private void addBlockOfTableForLicenseTypeHTML(
            StringBuilder htmlBuilder,
            Map<String, GroupInfo<?>> detectedLicenseInfo,
            String type,
            LPVSQueue webhookConfig,
            LPVSVcs vcs) {
        long detectedLicenseCountByType = getDetectedLicenseCountByType(detectedLicenseInfo, type);
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
            }
            htmlBuilder.append(" / ");
            htmlBuilder.append(getExplanationForLicenseType(type));
            htmlBuilder.append("</td>");

            // license spdx
            Map<String, GroupInfo<?>> licenseSpdxIds =
                    (Map<String, GroupInfo<?>>) detectedLicenseInfo.get(type).elements;
            for (String licenseSpdxId : licenseSpdxIds.keySet()) {
                if (!isNewRow) {
                    htmlBuilder.append("<tr>");
                    isNewRow = true;
                }
                htmlBuilder
                        .append("<td rowspan=\"")
                        .append(licenseSpdxIds.get(licenseSpdxId).getCount())
                        .append("\">");
                htmlBuilder.append(licenseSpdxId);
                htmlBuilder.append("</td>");

                // vendor + component
                Map<String, GroupInfo<?>> componentAndVendor =
                        (Map<String, GroupInfo<?>>) licenseSpdxIds.get(licenseSpdxId).elements;
                for (String componentInfo : componentAndVendor.keySet()) {
                    if (!isNewRow) {
                        htmlBuilder.append("<tr>");
                        isNewRow = true;
                    }

                    htmlBuilder
                            .append("<td rowspan=\"")
                            .append(componentAndVendor.get(componentInfo).getCount())
                            .append("\">");
                    htmlBuilder
                            .append("<a href=\"")
                            .append(componentInfo.split(":::")[1])
                            .append("\">")
                            .append(componentInfo.split(":::")[0])
                            .append("</a>");
                    htmlBuilder.append("</td>");

                    // file info
                    List<LPVSFile> fileInfos =
                            (List<LPVSFile>) componentAndVendor.get(componentInfo).elements;
                    for (LPVSFile fileInfo : fileInfos) {
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
                                .append(getMatchedLinesAsLink(webhookConfig, fileInfo, vcs))
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
            case permitted:
                return "This license permits free usage, modification, and distribution of the licensed code without any restrictions.";
            case unreviewed:
            default:
                return "This license has not been reviewed thoroughly and may contain unknown risks or limitations. It is recommended to review these licenses carefully before using the licensed code.";
        }
    }

    /**
     * Function that returns the number of licenses detected for the given type.
     *
     * @param detectedLicenseInfo grouped scan results by license SPDX ID and access type, component name and vendor
     * @param type the license type to count
     * @return the number of licenses detected for the given type
     */
    private long getDetectedLicenseCountByType(
            Map<String, GroupInfo<?>> detectedLicenseInfo, String type) {
        if (detectedLicenseInfo == null || detectedLicenseInfo.get(type) == null) {
            return 0;
        }
        return ((Map<String, GroupInfo<?>>) detectedLicenseInfo.get(type).elements).size();
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
     * Groups the scan results by license type for display in the license table.
     *
     * @param scanResults the results of the license scan
     */
    private Map<String, GroupInfo<?>> groupScanResultsForLicenseTable(List<LPVSFile> scanResults) {
        Map<String, GroupInfo<?>> detectedLicenseInfo = null;
        if (scanResults != null && !scanResults.isEmpty()) {
            List<LPVSFile> filesScanResults = getLpvsFilesFromScanResults(scanResults);

            detectedLicenseInfo =
                    filesScanResults.stream()
                            .collect(
                                    Collectors.groupingBy(
                                            this::getLicenseAccess,
                                            Collectors.collectingAndThen(
                                                    Collectors.groupingBy(
                                                            this::getLicenseSpdxId,
                                                            Collectors.collectingAndThen(
                                                                    Collectors.groupingBy(
                                                                            this::getComponentKey,
                                                                            Collectors
                                                                                    .collectingAndThen(
                                                                                            Collectors
                                                                                                    .toList(),
                                                                                            files ->
                                                                                                    new GroupInfo<>(
                                                                                                            files
                                                                                                                    .size(),
                                                                                                            files))),
                                                                    this::sumGroupInfo)),
                                                    this::sumGroupInfo)));
        }
        return detectedLicenseInfo;
    }

    /**
     * Sums the counts of all GroupInfo objects in the given map and returns a new GroupInfo object
     * containing the total count and the original map.
     *
     * @param groupedBy a map of strings to GroupInfo objects
     * @return a new GroupInfo object containing the total count of all GroupInfo objects in the map
     *         and the original map
     */
    private GroupInfo<Object> sumGroupInfo(Map<String, GroupInfo<Object>> groupedBy) {
        return new GroupInfo<>(
                groupedBy.values().stream().mapToLong(GroupInfo::getCount).sum(), groupedBy);
    }

    /**
     * Grouping criteria for the components
     *
     * @param lpvsFile the LPVSFile whose license SPDX ID is to be retrieved
     * @return the component key that contains component name, vendor name and component URL
     */
    private String getComponentKey(LPVSFile lpvsFile) {
        return lpvsFile.getComponentVendor()
                + " / "
                + lpvsFile.getComponentName()
                + ":::"
                + lpvsFile.getComponentUrl();
    }

    /**
     * Grouping criteria for the license SPDX ID
     *
     * @param lpvsFile the LPVSFile whose license SPDX ID is to be retrieved
     * @return the SPDX ID of the license
     */
    private String getLicenseSpdxId(LPVSFile lpvsFile) {
        return lpvsFile.getLicenses().stream().findFirst().get().getSpdxId();
    }

    /**
     * Grouping criteria for the license access type
     *
     * @param lpvsFile the LPVSFile whose license SPDX ID is to be retrieved
     * @return the access type of the license
     */
    private String getLicenseAccess(LPVSFile lpvsFile) {
        return lpvsFile.getLicenses().stream().findFirst().get().getAccess().toUpperCase();
    }
}
