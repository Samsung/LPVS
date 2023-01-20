/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import javax.persistence.*;

@Entity
@Table(name = "licenses", schema = "lpvs", indexes = {@Index(name = "spdx_id", columnList = "license_spdx", unique = true)})
public class LPVSLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long licenseId;

    @Column(name = "license_name", nullable = false)
    private String licenseName;

    @Column(name = "license_spdx", nullable = false)
    private String spdxId;

    @Column(name = "license_usage")
    private String access;

    @Transient
    private String checklistUrl;

    public LPVSLicense() {
    }

    public LPVSLicense(Long licenseId, String licenseName, String spdxId, String access, String checklistUrl) {
        this.licenseId = licenseId;
        this.licenseName = licenseName;
        this.spdxId = spdxId;
        this.access = access;
        this.checklistUrl = checklistUrl;
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

    public String getChecklistUrl() {
        return checklistUrl;
    }

    public void setChecklistUrl(String checklistUrl) {
        this.checklistUrl = checklistUrl;
    }

}
