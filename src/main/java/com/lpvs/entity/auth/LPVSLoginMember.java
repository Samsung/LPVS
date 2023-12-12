/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the login status and associated member information in the LPVS system.
 * This class provides a convenient way to encapsulate the login status and the member details.
 */
@Getter
@Setter
@AllArgsConstructor
public class LPVSLoginMember {

    /**
     * Indicates whether the member is currently logged in.
     */
    private Boolean isLoggedIn;

    /**
     * The member information associated with the login status.
     */
    private LPVSMember member;
}
