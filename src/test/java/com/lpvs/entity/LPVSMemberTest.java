/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity;

import com.lpvs.entity.auth.LPVSMember;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPVSMemberTest {

    @Test
    public void testLPVSMemberConstructor() {
        Long id = 1L;
        String name = "John Doe";
        String email = "john@example.com";
        String provider = "GitHub";
        String nickname = "johndoe";

        LPVSMember member =
                LPVSMember.builder()
                        .id(id)
                        .name(name)
                        .email(email)
                        .provider(provider)
                        .nickname(nickname)
                        .build();

        assertEquals(id, member.getId());
        assertEquals(name, member.getName());
        assertEquals(email, member.getEmail());
        assertEquals(provider, member.getProvider());
        assertEquals(nickname, member.getNickname());
    }

    @Test
    public void testLPVSMemberUpdate() {
        LPVSMember member =
                LPVSMember.builder()
                        .id(1L)
                        .name("Alice Johnson")
                        .email("alice@example.com")
                        .provider("GitHub")
                        .nickname("alicej")
                        .build();

        member.update("Bob Smith", "bob@example.com");
        assertEquals("Bob Smith", member.getName());
        assertEquals("bob@example.com", member.getEmail());
    }

    @Test
    public void testSetNickname() {
        LPVSMember member =
                LPVSMember.builder()
                        .id(1L)
                        .name("Charlie Brown")
                        .email("charlie@example.com")
                        .provider("GitHub")
                        .nickname("charlieb")
                        .build();

        member.setNickname("charliebrown");
        assertEquals("charliebrown", member.getNickname());
    }

    @Test
    public void testSetOrganization() {
        LPVSMember member =
                LPVSMember.builder()
                        .id(1L)
                        .name("David Smith")
                        .email("david@example.com")
                        .provider("GitHub")
                        .nickname("charlieb")
                        .build();

        member.setOrganization("Org");
        assertEquals("Org", member.getOrganization());
    }
}
