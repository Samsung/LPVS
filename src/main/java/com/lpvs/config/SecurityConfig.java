/**
 * Copyright (c) 2023, Basaeng, kyudori, hwan5180, quswjdgma83
 * Copyright (c) 2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.config;

import com.lpvs.service.OAuthService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Configuration class for setting up security configurations, including OAuth2 authentication
 * and Cross-Origin Resource Sharing (CORS) support.
 *
 * <p>
 * This class is responsible for configuring security settings using the provided OAuthService,
 * frontendMainPageUrl, and corsAllowedOrigin. It orchestrates the setup of security filters,
 * OAuth2 integration, and CORS support through Spring Security.
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Service for handling OAuth2 authentication.
     */
    private final OAuthService oAuthService;

    /**
     * The URL to redirect to after a successful logout.
     */
    @Value("${frontend.main-page.url:/}")
    private String frontendMainPageUrl;

    /**
     * The allowed origin for CORS. Defaults to an empty string.
     */
    @Value("${cors.allowed-origin:}")
    private String corsAllowedOrigin;

    /**
     * Configures the security filter chain.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(
                        cors ->
                                cors.configurationSource(
                                        request ->
                                                new CorsConfiguration().applyPermitDefaultValues()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(
                        headers ->
                                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .logout(
                        logout ->
                                logout.logoutRequestMatcher(
                                                new AntPathRequestMatcher("/oauth/logout"))
                                        .logoutSuccessUrl(frontendMainPageUrl)
                                        .invalidateHttpSession(true)
                                        .clearAuthentication(true))
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .oauth2Login(
                        login ->
                                login.successHandler(
                                                new AuthenticationSuccessHandler() {
                                                    @Value("${frontend.main-page.url:}")
                                                    private String frontendMainPageUrl;

                                                    private final String REDIRECT_URI =
                                                            frontendMainPageUrl + "/login/callback";

                                                    @Override
                                                    public void onAuthenticationSuccess(
                                                            HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            Authentication authentication)
                                                            throws IOException {
                                                        response.sendRedirect(
                                                                UriComponentsBuilder.fromUriString(
                                                                                REDIRECT_URI)
                                                                        .queryParam(
                                                                                "accessToken",
                                                                                "accessToken")
                                                                        .queryParam(
                                                                                "refreshToken",
                                                                                "refreshToken")
                                                                        .build()
                                                                        .encode(
                                                                                StandardCharsets
                                                                                        .UTF_8)
                                                                        .toUriString());
                                                    }
                                                })
                                        .defaultSuccessUrl(frontendMainPageUrl, true)
                                        .userInfoEndpoint(
                                                userInfo -> userInfo.userService(oAuthService)));

        return http.build();
    }

    /**
     * Configures the CORS (Cross-Origin Resource Sharing) support.
     *
     * @return The CorsConfigurationSource containing CORS configurations.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(corsAllowedOrigin);
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Defines a simple ClientRegistrationRepository that always returns null for any registration ID.
     *
     * @return ClientRegistrationRepository bean.
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return registrationId -> null;
    }
}
