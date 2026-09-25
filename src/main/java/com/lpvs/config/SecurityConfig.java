/**
 * Copyright (c) 2023-2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                        authorizeRequests ->
                                authorizeRequests
                                        // GitHub webhooks, authenticated by HMAC signature
                                        // in GitHubController
                                        .requestMatchers(HttpMethod.POST, "/", "/webhooks")
                                        .permitAll()
                                        // Single scan API, authenticated by API key
                                        // in GitHubController
                                        .requestMatchers(HttpMethod.POST, "/scan/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, "/health")
                                        .permitAll()
                                        .requestMatchers("/error")
                                        .permitAll()
                                        .anyRequest()
                                        .denyAll())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Stateless API without sessions or cookies, so CSRF protection is not applicable
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
