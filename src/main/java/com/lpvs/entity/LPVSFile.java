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

import java.util.Set;

/**
 * Represents a file in the LPVS system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
        String licenseNames = "";
        for (LPVSLicense license : this.licenses) {
            String licSpdxId = license.getSpdxId();
            // Check if the license SPDX ID has scanner-specific name
            if (licSpdxId.startsWith("LicenseRef")) {
                // Change the name of the license that will be displayed in PR comment to
                // scanner-independent
                licSpdxId =
                        "UNREVIEWED LICENSE : "
                                + licSpdxId
                                        .replaceAll("LicenseRef-scancode-", "")
                                        .replaceAll("LicenseRef-scanoss-", "");
            }
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                licenseNames +=
                        (license.getChecklistUrl() != null
                                        ? "<a target=\"_blank\" href=\""
                                                + license.getChecklistUrl()
                                                + "\">"
                                        : "")
                                + licSpdxId
                                + (license.getChecklistUrl() != null ? "</a>" : "")
                                + " ("
                                + license.getAccess().toLowerCase()
                                + "), ";
            } else {
                licenseNames +=
                        licSpdxId
                                + (license.getChecklistUrl() != null
                                        ? " (" + license.getChecklistUrl() + ")"
                                        : "")
                                + " - "
                                + license.getAccess().toLowerCase()
                                + ", ";
            }
        }
        if (licenseNames.endsWith(", "))
            licenseNames = licenseNames.substring(0, licenseNames.length() - 2);
        return licenseNames;
    }
}
