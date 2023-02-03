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

import javax.persistence.*;

@Entity
@Table(name = "license_conflicts", schema = "lpvs")
@Getter @Setter @NoArgsConstructor
public class LPVSLicenseConflict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long conflictId;

    @ManyToOne
    @JoinColumn(name = "repository_license", referencedColumnName = "id", nullable = false)
    private LPVSLicense repositoryLicense;

    @ManyToOne
    @JoinColumn(name = "conflict_license", referencedColumnName = "id", nullable = false)
    private LPVSLicense conflictLicense;

    public LPVSLicenseConflict(LPVSLicense repositoryLicense, LPVSLicense conflictLicense) {
        this.repositoryLicense = repositoryLicense;
        this.conflictLicense = conflictLicense;
    }
}
