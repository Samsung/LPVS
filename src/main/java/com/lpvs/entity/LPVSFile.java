/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import java.util.Set;

public class LPVSFile {

    private Long id;
    private String fileUrl;
    private String filePath;
    private String snippetMatch;
    private String matchedLines;
    private Set<LPVSLicense> licenses;
    private String component;

    public LPVSFile() {
    }

    public LPVSFile(Long id, String fileUrl, String filePath, String snippetMatch, String matchedLines, Set<LPVSLicense> licenses, String component) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.snippetMatch = snippetMatch;
        this.matchedLines = matchedLines;
        this.licenses = licenses;
        this.component = component;
    }

    public Set<LPVSLicense> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<LPVSLicense> licenses) {
        this.licenses = licenses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSnippetMatch() {
        return snippetMatch;
    }

    public void setSnippetMatch(String snippetMatch) {
        this.snippetMatch = snippetMatch;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getMatchedLines() {
        return matchedLines;
    }

    public void setMatchedLines(String matchedLines) {
        this.matchedLines = matchedLines;
    }

    public String convertLicensesToString() {
        String licenseNames = "";
        for (LPVSLicense license : this.licenses) {
            licenseNames += (license.getChecklist_url() != null ? "<a target=\"_blank\" href=\"" + license.getChecklist_url() + "\">" : "") +
                    license.getSpdxId() +
                    (license.getChecklist_url() != null ? "</a>" : "") +
                    " (" + license.getAccess().toLowerCase() + "), ";
        }
        if (licenseNames.endsWith(", ")) licenseNames = licenseNames.substring(0, licenseNames.length() - 2);
        return licenseNames;
    }
}