/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "detected_license", schema = "lpvs")
@Getter @Setter
public class LPVSDetectedLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pull_request_id", referencedColumnName = "id", nullable = false)
    private LPVSPullRequest pullRequest;

    @ManyToOne
    @JoinColumn(name = "license_id", referencedColumnName = "id", nullable = false)
    private LPVSLicense license;

    @ManyToOne
    @JoinColumn(name = "conflict_id", referencedColumnName = "id", nullable = false)
    private LPVSLicenseConflict licenseConflict;

    @ManyToOne
    @JoinColumn(name = "repository_license_id", referencedColumnName = "id", nullable = false)
    private LPVSLicense repositoryLicense;

    @Column(name = "file_path",  columnDefinition = "LONGTEXT")
    private String filePath;

    @Column(name = "type")
    private String type;

    @Column(name = "match")
    private String match;

    @Column(name = "lines")
    private String lines;

    @Column(name = "component_name")
    private String componentFilePath;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "component_lines")
    private String componentLines;

    @Column(name = "component_url")
    private String componentUrl;

    @Column(name = "version")
    private String componentVersion;

    @Column(name = "vendor")
    private String componentVendor;

    @Column(name = "issue")
    private Boolean issue;

}
