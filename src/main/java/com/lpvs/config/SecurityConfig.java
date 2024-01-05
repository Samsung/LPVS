/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.config;

import com.lpvs.service.OAuthService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        http.cors()
                .and()
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable()
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/oauth/logout"))
                .logoutSuccessUrl(frontendMainPageUrl)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .and()
                .authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .oauth2Login()
                .successHandler(
                        new AuthenticationSuccessHandler() {
                            @Value("${frontend.main-page.url:}")
                            private String frontendMainPageUrl;

                            private String REDIRECT_URI = frontendMainPageUrl + "/login/callback";

                            @Override
                            public void onAuthenticationSuccess(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication)
                                    throws IOException, ServletException {
                                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                                System.out.println("oAuth2User = " + oAuth2User);

                                response.sendRedirect(
                                        UriComponentsBuilder.fromUriString(REDIRECT_URI)
                                                .queryParam("accessToken", "accessToken")
                                                .queryParam("refreshToken", "refreshToken")
                                                .build()
                                                .encode(StandardCharsets.UTF_8)
                                                .toUriString());
                            }
                        })
                .defaultSuccessUrl(frontendMainPageUrl, true)
                .userInfoEndpoint()
                .userService(oAuthService);

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
}
