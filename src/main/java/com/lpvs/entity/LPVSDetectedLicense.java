/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entity class representing detected licenses in the LPVS system.
 * This class is mapped to the "detected_license" table in the "lpvs" schema.
 */
@Entity
@Table(name = "detected_license", schema = "lpvs")
@Getter
@Setter
public class LPVSDetectedLicense implements Serializable {

    /**
     * Unique identifier for the detected license.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The pull request associated with the detected license.
     */
    @ManyToOne
    @JoinColumn(name = "pull_request_id", referencedColumnName = "id")
    private LPVSPullRequest pullRequest = null;

    /**
     * The license associated with the detected license.
     */
    @ManyToOne
    @JoinColumn(name = "license_id", referencedColumnName = "id")
    private LPVSLicense license = null;

    /**
     * The license conflict associated with the detected license.
     */
    @ManyToOne
    @JoinColumn(name = "conflict_id", referencedColumnName = "id")
    private LPVSLicenseConflict licenseConflict = null;

    /**
     * The repository license associated with the detected license.
     */
    @ManyToOne
    @JoinColumn(name = "repository_license_id", referencedColumnName = "id")
    private LPVSLicense repositoryLicense = null;

    /**
     * The file path associated with the detected license.
     */
    @Column(name = "file_path", columnDefinition = "LONGTEXT")
    private String filePath = null;

    /**
     * The type of license match.
     */
    @Column(name = "match_type")
    private String type = null;

    /**
     * The value of the license match.
     */
    @Column(name = "match_value")
    private String match = null;

    /**
     * The lines where the license is matched.
     */
    @Column(name = "match_lines")
    private String lines = null;

    /**
     * The file path associated with the component.
     */
    @Column(name = "component_file_path", columnDefinition = "LONGTEXT")
    private String componentFilePath = null;

    /**
     * The URL of the file path associated with the component.
     */
    @Column(name = "component_file_url", columnDefinition = "LONGTEXT")
    private String componentFileUrl = null;

    /**
     * The name of the component.
     */
    @Column(name = "component_name")
    private String componentName = null;

    /**
     * The lines associated with the component.
     */
    @Column(name = "component_lines")
    private String componentLines = null;

    /**
     * The URL associated with the component.
     */
    @Column(name = "component_url")
    private String componentUrl = null;

    /**
     * The version of the component.
     */
    @Column(name = "component_version")
    private String componentVersion = null;

    /**
     * The vendor of the component.
     */
    @Column(name = "component_vendor")
    private String componentVendor = null;

    /**
     * Indicates whether there is an issue related to the detected license.
     */
    @Column(name = "issue")
    private Boolean issue = false;
}
