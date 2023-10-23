/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

public class OAuthAttributesTest {

    @Test
    public void testExtractOAuthAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>() {{
            put("name", "testName");
            put("email", "testEmail");
        }};;
        MemberProfile profile = OAuthAttributes.extract("google", attributes);
        assertEquals("testName", profile.getName());
        assertEquals("testEmail", profile.getEmail());
    }

    @Test
    public void testExtractOAuthAttributesUnknownProvider() {
        Map<String, Object> attributes = new HashMap<String, Object>() {{
            put("name", "testName");
            put("email", "testEmail");
        }};;
        assertThrows(IllegalArgumentException.class, () -> {
            OAuthAttributes.extract("unknown", attributes);
        });
    }
}
