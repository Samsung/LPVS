/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "detected_license", schema = "lpvs")
@Getter @Setter
public class LPVSDetectedLicense implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pull_request_id", referencedColumnName = "id")
    private LPVSPullRequest pullRequest = null;

    @ManyToOne
    @JoinColumn(name = "license_id", referencedColumnName = "id")
    private LPVSLicense license = null;

    @ManyToOne
    @JoinColumn(name = "conflict_id", referencedColumnName = "id")
    private LPVSLicenseConflict licenseConflict = null;

    @ManyToOne
    @JoinColumn(name = "repository_license_id", referencedColumnName = "id")
    private LPVSLicense repositoryLicense = null;

    @Column(name = "file_path",  columnDefinition = "LONGTEXT")
    private String filePath = null;

    @Column(name = "match_type")
    private String type = null;

    @Column(name = "match_value")
    private String match = null;

    @Column(name = "match_lines")
    private String lines = null;

    @Column(name = "component_file_path", columnDefinition = "LONGTEXT")
    private String componentFilePath = null;

    @Column(name = "component_file_url", columnDefinition = "LONGTEXT")
    private String componentFileUrl = null;

    @Column(name = "component_name")
    private String componentName = null;

    @Column(name = "component_lines")
    private String componentLines = null;

    @Column(name = "component_url")
    private String componentUrl = null;

    @Column(name = "component_version")
    private String componentVersion = null;

    @Column(name = "component_vendor")
    private String componentVendor = null;

    @Column(name = "issue")
    private Boolean issue = false;

}
