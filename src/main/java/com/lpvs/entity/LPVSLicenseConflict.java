/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity;

import javax.persistence.*;

@Entity
@Table(name = "license_conflicts", schema = "lpvs")
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

    public LPVSLicenseConflict() { }

    public Long getConflictId() { return conflictId; }

    public void setConflictId(Long conflictId) { this.conflictId = conflictId; }

    public LPVSLicense getRepositoryLicense() { return repositoryLicense; }

    public void setRepositoryLicense(LPVSLicense repositoryLicense) { this.repositoryLicense = repositoryLicense; }

    public LPVSLicense getConflictLicense() { return conflictLicense; }

    public void setConflictLicense(LPVSLicense conflictLicense) { this.conflictLicense = conflictLicense; }
}
