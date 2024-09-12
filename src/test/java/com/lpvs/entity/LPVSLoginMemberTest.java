/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import com.lpvs.entity.auth.LPVSLoginMember;
import com.lpvs.entity.auth.LPVSMember;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LPVSLoginMemberTest {

    @Test
    public void testLPVSLoginMemberConstructor() {
        Boolean isLoggedIn = true;
        LPVSMember member = new LPVSMember(1L, "John", "john@example.com", "provider", "nickname");
        LPVSLoginMember loginMember = new LPVSLoginMember(isLoggedIn, member);
        assertEquals(isLoggedIn, loginMember.getIsLoggedIn());
        assertEquals(member, loginMember.getMember());
    }

    @Test
    public void testLPVSLoginMemberGettersAndSetters() {
        LPVSLoginMember loginMember =
                new LPVSLoginMember(
                        false,
                        new LPVSMember(1L, "John", "john@email.com", "provider", "nickname"));
        loginMember.setIsLoggedIn(true);
        LPVSMember newMember = new LPVSMember(1L, "John", "john@email.com", "provider", "nickname");
        loginMember.setMember(newMember);
        assertTrue(loginMember.getIsLoggedIn());
        assertEquals(newMember, loginMember.getMember());
    }
}
