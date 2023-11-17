/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

/**
 * This package contains repository interfaces responsible for managing data access and persistence for the application.
 * Each repository interface typically extends {@link org.springframework.data.jpa.repository.JpaRepository}
 * for basic CRUD operations and may include additional methods for custom queries and data retrieval.
 *
 * <p>
 * The repositories in this package interact with the underlying database to perform operations on entities
 * such as {@link com.lpvs.entity.LPVSDetectedLicense}, {@link com.lpvs.entity.LPVSLicenseConflict},
 * {@link com.lpvs.entity.LPVSMember}, {@link com.lpvs.entity.LPVSQueue}, and {@link com.lpvs.entity.LPVSPullRequest}.
 * </p><p>
 * The custom queries defined in these repositories use native queries (SQL) or Spring Data JPA query language
 * to retrieve specific data sets based on various criteria.
 * </p>
 */
package com.lpvs.repository;
