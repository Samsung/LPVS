/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.auth;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a member profile in the LPVS system.
 * This class is used to encapsulate basic information about a member.
 */
@Getter
@Setter
public class MemberProfile {

    /**
     * The name of the member.
     */
    private String name;

    /**
     * The email address of the member.
     */
    private String email;

    /**
     * The provider associated with the member (e.g., OAuth provider).
     */
    private String provider;

    /**
     * The nickname of the member.
     */
    private String nickname;

    /**
     * Converts the member profile to an instance of {@link LPVSMember}.
     *
     * @return An instance of {@link LPVSMember} created from the member profile.
     */
    public LPVSMember toMember() {
        return LPVSMember.builder().name(name).email(email).provider(provider).build();
    }
}
