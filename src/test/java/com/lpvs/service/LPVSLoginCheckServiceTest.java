/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSMember;
import com.lpvs.entity.history.HistoryPageEntity;
import com.lpvs.exception.LoginFailedException;
import com.lpvs.exception.WrongAccessException;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LPVSLoginCheckServiceTest {

    @Mock private LPVSPullRequestRepository lpvsPullRequestRepository;

    @Mock private LPVSMemberRepository memberRepository;

    @Mock private Authentication authentication;

    private LPVSLoginCheckService loginCheckService;

    @BeforeEach
    public void setUp() {
        lpvsPullRequestRepository = mock(LPVSPullRequestRepository.class);
        memberRepository = mock(LPVSMemberRepository.class);
        authentication = mock(Authentication.class);

        loginCheckService = new LPVSLoginCheckService(lpvsPullRequestRepository, memberRepository);
    }

    @Test
    public void testGetOauthLoginMemberMap() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = Collections.singletonMap("email", "test@example.com");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        Map<String, Object> result = loginCheckService.getOauthLoginMemberMap(authentication);

        assertNotNull(result);
        assertEquals("test@example.com", result.get("email"));
    }

    @Test
    public void testGetOauthLoginMemberMapWithNullPrincipal() {
        when(authentication.getPrincipal()).thenReturn(null);

        Map<String, Object> result = loginCheckService.getOauthLoginMemberMap(authentication);

        assertNull(result);
    }

    @Test
    public void testLoginVerificationWithNullPrincipal() {
        when(authentication.getPrincipal()).thenReturn(null);

        assertThrows(
                LoginFailedException.class,
                () -> loginCheckService.loginVerification(authentication));
    }

    @Test
    public void testLoginVerificationWithEmptyPrincipal() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(new LinkedHashMap<>());

        assertThrows(
                LoginFailedException.class,
                () -> loginCheckService.loginVerification(authentication));
    }

    @Test
    public void testGetMemberFromMemberMap() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("provider", "provider");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        when(memberRepository.findByEmailAndProvider("test@example.com", "provider"))
                .thenReturn(Optional.of(new LPVSMember()));

        LPVSMember result = loginCheckService.getMemberFromMemberMap(authentication);

        assertNotNull(result);
    }

    @Test
    public void testPathCheckAllTypes() {
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("provider", "provider");
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(lpvsPullRequestRepository.findByPullRequestBase("testNickname", null))
                .thenReturn(Page.empty());
        when(memberRepository.findByEmailAndProvider("test@example.com", "provider"))
                .thenReturn(Optional.of(member));

        HistoryPageEntity result =
                loginCheckService.pathCheck("own", "testNickname", null, authentication);
        assertNotNull(result);

        result = loginCheckService.pathCheck("send", "testNickname", null, authentication);
        assertNotNull(result);

        assertThrows(
                WrongAccessException.class,
                () -> {
                    loginCheckService.pathCheck("test", "testNickname", null, authentication);
                });
    }
}
