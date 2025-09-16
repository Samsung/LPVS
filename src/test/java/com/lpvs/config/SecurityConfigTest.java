/**
 * Copyright (c) 2023-2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SecurityConfig.class)
class SecurityConfigTest {

    @Autowired private SecurityConfig securityConfig;

    @Autowired private SecurityFilterChain securityFilterChain;

    @Test
    void testContextLoads() {
        // Verify that the SecurityConfig bean is created and loaded
        assertNotNull(securityConfig);
    }

    @Test
    void securityFilterChain_ShouldBeConfiguredCorrectly() {
        // Verify that the SecurityFilterChain bean is created
        assertNotNull(securityFilterChain);
    }
}
