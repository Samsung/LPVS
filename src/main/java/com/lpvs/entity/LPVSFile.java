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

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class LPVSFile {

    private Long id;
    private String filePath;
    private String snippetType;
    private String snippetMatch; // in %
    private String matchedLines;
    private Set<LPVSLicense> licenses;
    private String componentFilePath;
    private String componentFileUrl;
    private String componentName;
    private String componentLines;
    private String componentUrl;
    private String componentVersion;
    private String componentVendor;

    public String convertLicensesToString(LPVSVcs vcs) {
        String licenseNames = "";
        for (LPVSLicense license : this.licenses) {
            String licSpdxId = license.getSpdxId();
            // Check if the license SPDX ID has scanner-specific name
            if (licSpdxId.startsWith("LicenseRef")) {
                // Change the name of the license that will be displayed in PR comment to scanner-independent
                licSpdxId = "UNREVIEWED LICENSE : " + licSpdxId.replaceAll("LicenseRef-scancode-", "").replaceAll("LicenseRef-scanoss-", "");
            }
            if (vcs != null && vcs.equals(LPVSVcs.GITHUB)) {
                licenseNames += (license.getChecklistUrl() != null ? "<a target=\"_blank\" href=\"" + license.getChecklistUrl() + "\">" : "") +
                        licSpdxId +
                        (license.getChecklistUrl() != null ? "</a>" : "") +
                        " (" + license.getAccess().toLowerCase() + "), ";
            } else {
                licenseNames += licSpdxId + (license.getChecklistUrl() != null ? " (" + license.getChecklistUrl() + ")" : "") +
                        " - " + license.getAccess().toLowerCase() + ", ";
            }
        }
        if (licenseNames.endsWith(", ")) licenseNames = licenseNames.substring(0, licenseNames.length() - 2);
        return licenseNames;
    }
}