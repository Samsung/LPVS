/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import com.lpvs.entity.enums.LPVSVcs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a file in the LPVS system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class LPVSFile {

    /**
     * The unique identifier for the file.
     */
    private Long id;

    /**
     * The path of the file.
     */
    private String filePath;

    /**
     * The absolute path to the file on the server.
     */
    private String absoluteFilePath;

    /**
     * The type of snippet in the file.
     */
    private String snippetType;

    /**
     * The percentage match of the snippet in the file.
     */
    private String snippetMatch;

    /**
     * The matched lines in the file.
     */
    private String matchedLines;

    /**
     * Set of licenses associated with the file.
     */
    private Set<LPVSLicense> licenses;

    /**
     * The file path associated with the component.
     */
    private String componentFilePath;

    /**
     * The URL of the file path associated with the component.
     */
    private String componentFileUrl;

    /**
     * The name of the component.
     */
    private String componentName;

    /**
     * The lines associated with the component.
     */
    private String componentLines;

    /**
     * The URL associated with the component.
     */
    private String componentUrl;

    /**
     * The version of the component.
     */
    private String componentVersion;

    /**
     * The vendor of the component.
     */
    private String componentVendor;

    /**
     * Converts the set of licenses to a formatted string.
     *
     * @param vcs The version control system.
     * @return A formatted string representing the licenses.
     */
    public String convertLicensesToString(LPVSVcs vcs) {
        StringBuilder licenseNames = new StringBuilder();
        Map<String, List<LPVSLicense>> groupedLicenses =
                licenses.stream().collect(Collectors.groupingBy(LPVSLicense::getAccess));

        for (Map.Entry<String, List<LPVSLicense>> entry : groupedLicenses.entrySet()) {
            String accessType = entry.getKey();
            List<LPVSLicense> licensesWithAccessType = entry.getValue();
            licenseNames.append("\n").append("- ").append(accessType.toUpperCase()).append(":\n");

            for (LPVSLicense license : licensesWithAccessType) {
                String licSpdxId = license.getSpdxId();
                // Check if the license SPDX ID has scanner-specific name.
                // Change the name of the license that will be displayed in PR comment to
                // scanner-independent
                licSpdxId =
                        licSpdxId
                                .replaceAll("LicenseRef-scancode-", "")
                                .replaceAll("LicenseRef-scanoss-", "");

                if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                    licenseNames
                            .append("  : ")
                            .append(
                                    license.getChecklistUrl() != null
                                            ? "<a target=\"_blank\" href=\""
                                                    + license.getChecklistUrl()
                                                    + "\">"
                                            : "")
                            .append(licSpdxId)
                            .append(license.getChecklistUrl() != null ? "</a>" : "")
                            .append("\n");
                } else {
                    licenseNames
                            .append("  : ")
                            .append(licSpdxId)
                            .append(
                                    license.getChecklistUrl() != null
                                            ? " (" + license.getChecklistUrl() + ")"
                                            : "")
                            .append("\n");
                }
            }
        }

        return licenseNames.toString();
    }

    /**
     * Converts byte ranges to line numbers in a file.
     *
     * @return A string representing the start and end line numbers corresponding to the byte ranges.
     *         Returns an empty string if the input does not start with "BYTES:" or if an error occurs.
     */
    public String convertBytesToLinesNumbers() {

        if (matchedLines == null || matchedLines.isEmpty()) {
            return "";
        } else if (!matchedLines.startsWith("BYTES:")) {
            return matchedLines;
        }

        StringBuilder result = new StringBuilder();

        try (RandomAccessFile sourceFile = new RandomAccessFile(absoluteFilePath, "r")) {
            // Skip "BYTES:" before splitting
            String[] byteRangeTokens = matchedLines.substring(6).split(":");

            for (String byteRangeToken : byteRangeTokens) {
                String[] range = byteRangeToken.split("-");
                int startByte = Integer.parseInt(range[0]);
                int endByte = Integer.parseInt(range[1]);

                // Read lines until reaching the end byte
                long byteCounter = 0;
                long currentLine = 1;
                long startLine = 0;
                long endLine = 0;

                // Reset file pointer to the beginning of the file
                sourceFile.seek(0);

                while (byteCounter < endByte) {
                    String line = sourceFile.readLine();
                    if (line == null) {
                        if (startLine > 0) endLine = currentLine;
                        break;
                    }
                    byteCounter += line.getBytes(StandardCharsets.UTF_8).length + 1;
                    if (byteCounter > startByte && startLine == 0) {
                        startLine = currentLine;
                    }
                    if (byteCounter >= endByte) {
                        endLine = currentLine;
                        break;
                    }
                    currentLine++;
                }

                // Construct the string representing start and end line numbers in the range
                if (!result.isEmpty()) {
                    result.append(",");
                }
                result.append(startLine);
                result.append("-");
                result.append(endLine);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return "";
        }

        return result.toString();
    }
}
