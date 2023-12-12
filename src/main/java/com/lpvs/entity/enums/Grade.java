/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

/**
 * Represents the severity grades for license violations in the LPVS system.
 * The grades indicate the seriousness or impact of the detected license violation.
 */
public enum Grade {

    /**
     * Represents a serious license violation with high impact.
     */
    SERIOUS,

    /**
     * Represents a high severity license violation.
     */
    HIGH,

    /**
     * Represents a license violation with moderate severity.
     */
    MIDDLE,

    /**
     * Represents a license violation with low severity.
     */
    LOW,

    /**
     * Represents no license violation or the absence of severity.
     */
    NONE
}
