/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.auth;

import com.lpvs.entity.LPVSMember;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberProfile {
    private String name;
    private String email;
    private String provider;
    private String nickname;


    public LPVSMember toMember() {
        return LPVSMember.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .build();
    }

}
