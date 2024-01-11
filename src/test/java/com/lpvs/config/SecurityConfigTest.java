/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.config;

import com.lpvs.service.OAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SecurityConfig.class)
class SecurityConfigTest {

    @Autowired private SecurityConfig securityConfig;

    @MockBean private OAuthService oAuthService;

    @MockBean private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void testContextLoads() {
        // Verify that the SecurityConfig bean is created and loaded
        assertNotNull(securityConfig);
    }

    @Test
    public void testOnAuthenticationSuccess() throws IOException, ServletException {
        AuthenticationSuccessHandler successHandler = mock(AuthenticationSuccessHandler.class);

        // Configure the behavior of onAuthenticationSuccess
        doAnswer(
                        invocation -> {
                            HttpServletRequest request = invocation.getArgument(0);
                            HttpServletResponse response = invocation.getArgument(1);
                            Authentication authentication = invocation.getArgument(2);
                            String frontendMainPageUrl = "/frontend-main-page-url";
                            String redirectUri =
                                    frontendMainPageUrl
                                            + "/login/callback?accessToken=accessToken&"
                                            + "refreshToken=refreshToken";
                            response.sendRedirect(redirectUri);
                            return null;
                        })
                .when(successHandler)
                .onAuthenticationSuccess(any(), any(), any());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);

        // Set up the authentication to return an OAuth2User
        OAuth2User oAuth2User =
                new DefaultOAuth2User(
                        Collections.singleton((GrantedAuthority) () -> "ROLE_USER"),
                        Collections.singletonMap("sub", "1234567890"),
                        "sub");
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        successHandler.onAuthenticationSuccess(request, response, authentication);
        String expectedRedirectUri =
                "/frontend-main-page-url/login/callback?accessToken=accessToken&"
                        + "refreshToken=refreshToken";
        verify(response).sendRedirect(expectedRedirectUri);
    }
}
