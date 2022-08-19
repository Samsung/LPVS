/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import java.util.List;

public class LPVSLicense {

    private Long licenseId;

    private String licenseName;

    private String spdxId;

    private String access;

    private String checklistUrl;

    private List<String> incompatibleWith;

    public LPVSLicense() {
    }

    public LPVSLicense(Long licenseId, String licenseName, String spdxId, String access, String checklistUrl, List<String> incompatibleWith) {
        this.licenseId = licenseId;
        this.licenseName = licenseName;
        this.spdxId = spdxId;
        this.access = access;
        this.checklistUrl = checklistUrl;
        this.incompatibleWith = incompatibleWith;
    }

    public Long getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Long licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getSpdxId() {
        return spdxId;
    }

    public void setSpdxId(String spdxId) {
        this.spdxId = spdxId;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getChecklist_url() {
        return checklistUrl;
    }

    public void setChecklist_url(String checklist_url) {
        this.checklistUrl = checklist_url;
    }

    public String getChecklistUrl() {
        return checklistUrl;
    }

    public void setChecklistUrl(String checklistUrl) {
        this.checklistUrl = checklistUrl;
    }

    public List<String> getIncompatibleWith() {
        return incompatibleWith;
    }

    public void setIncompatibleWith(List<String> incompatibleWith) {
        this.incompatibleWith = incompatibleWith;
    }

}
