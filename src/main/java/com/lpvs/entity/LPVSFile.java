/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

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
    private String componentName;
    private String componentLines;
    private String componentUrl;
    private String componentVersion;
    private String componentVendor;

    public String convertLicensesToString() {
        String licenseNames = "";
        for (LPVSLicense license : this.licenses) {
            licenseNames += (license.getChecklistUrl() != null ? "<a target=\"_blank\" href=\"" + license.getChecklistUrl() + "\">" : "") +
                    license.getSpdxId() +
                    (license.getChecklistUrl() != null ? "</a>" : "") +
                    " (" + license.getAccess().toLowerCase() + "), ";
        }
        if (licenseNames.endsWith(", ")) licenseNames = licenseNames.substring(0, licenseNames.length() - 2);
        return licenseNames;
    }
}