/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.auth;

import org.junit.jupiter.api.Test;

import com.lpvs.entity.LPVSMember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class MemberProfileTest {

    @Test
    public void testConstructor() {
        MemberProfile profile = new MemberProfile();
        assertNull(profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getProvider());
        assertNull(profile.getNickname());
    }

    @Test
    public void testToMember() {
        MemberProfile profile = new MemberProfile();
        profile.setName("John");
        profile.setEmail("john@example.com");
        profile.setProvider("OAuth2");
        profile.setNickname("Johnny");
        LPVSMember member = profile.toMember();

        assertEquals("John", member.getName());
        assertEquals("john@example.com", member.getEmail());
        assertEquals("OAuth2", member.getProvider());
        assertEquals("Johnny", profile.getNickname());
    }
}
