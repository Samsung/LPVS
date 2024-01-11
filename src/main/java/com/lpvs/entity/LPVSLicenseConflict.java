/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Represents a license conflict in the LPVS system.
 * This class is mapped to the "license_conflicts" table in the "lpvs" schema.
 */
@Entity
@Table(name = "license_conflicts", schema = "lpvs")
@Getter
@Setter
@NoArgsConstructor
public class LPVSLicenseConflict implements Serializable {

    /**
     * The unique identifier for the license conflict.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long conflictId;

    /**
     * The repository license involved in the conflict.
     */
    @ManyToOne
    @JoinColumn(name = "repository_license_id", referencedColumnName = "id", nullable = false)
    private LPVSLicense repositoryLicense;

    /**
     * The conflicting license involved in the conflict.
     */
    @ManyToOne
    @JoinColumn(name = "conflict_license_id", referencedColumnName = "id", nullable = false)
    private LPVSLicense conflictLicense;

    /**
     * Constructs a new license conflict with the given repository and conflicting licenses.
     *
     * @param repositoryLicense The repository license involved in the conflict.
     * @param conflictLicense   The conflicting license involved in the conflict.
     */
    public LPVSLicenseConflict(LPVSLicense repositoryLicense, LPVSLicense conflictLicense) {
        this.repositoryLicense = repositoryLicense;
        this.conflictLicense = conflictLicense;
    }
}
