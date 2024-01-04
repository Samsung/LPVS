/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entity class representing licenses in the LPVS system.
 * This class is mapped to the "licenses" table in the "lpvs" schema.
 */
@Entity
@Table(
        name = "licenses",
        schema = "lpvs",
        indexes = {@Index(name = "spdx_id", columnList = "license_spdx", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LPVSLicense implements Serializable {

    /**
     * Unique identifier for the license.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long licenseId;

    /**
     * The name of the license.
     */
    @Column(name = "license_name", nullable = false)
    private String licenseName;

    /**
     * The SPDX identifier of the license.
     */
    @Column(name = "license_spdx", nullable = false)
    private String spdxId;

    /**
     * The usage level associated with the license.
     */
    @Column(name = "license_usage")
    private String access;

    /**
     * Alternative names for the license.
     */
    @Column(name = "license_alternative_names", columnDefinition = "LONGTEXT")
    private String alternativeNames;

    /**
     * Transient field representing the URL for the license checklist.
     */
    @Transient private String checklistUrl;
}
