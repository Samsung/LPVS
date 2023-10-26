/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

import com.lpvs.entity.auth.MemberProfile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

public class OAuthAttributesTest {

    @Test
    public void testExtractOAuthAttributes() {
        Map<String, Object> attributesSub =
                new HashMap<String, Object>() {
                    {
                        put("name", "testName");
                        put("nickname", "testName");
                        put("email", "testEmail");
                    }
                };

        Map<String, Object> attributesKakao =
                new HashMap<String, Object>() {
                    {
                        put("email", "testEmail");
                        put("profile", attributesSub);
                    }
                };

        Map<String, Object> attributes =
                new HashMap<String, Object>() {
                    {
                        put("name", "testName");
                        put("email", "testEmail");
                        put("login", "testEmail");
                        put("response", attributesSub);
                        put("kakao_account", attributesKakao);
                    }
                };

        MemberProfile profileGoogle = OAuthAttributes.extract("google", attributes);
        assertEquals("testName", profileGoogle.getName());
        assertEquals("testEmail", profileGoogle.getEmail());

        MemberProfile profileNaver = OAuthAttributes.extract("naver", attributes);
        assertEquals("testName", profileNaver.getName());
        assertEquals("testEmail", profileNaver.getEmail());

        MemberProfile profileKakao = OAuthAttributes.extract("kakao", attributes);
        assertEquals("testName", profileKakao.getName());
        assertEquals("testEmail", profileKakao.getEmail());

        MemberProfile profileGithub = OAuthAttributes.extract("github", attributes);
        assertEquals("testName", profileGithub.getName());
        assertEquals("testEmail", profileGithub.getEmail());
    }

    @Test
    public void testExtractOAuthAttributesUnknownProvider() {
        Map<String, Object> attributes =
                new HashMap<String, Object>() {
                    {
                        put("name", "testName");
                        put("email", "testEmail");
                    }
                };
        ;
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    OAuthAttributes.extract("unknown", attributes);
                });
    }
}
