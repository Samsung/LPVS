/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSMember;
import com.lpvs.exception.LoginFailedException;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class LPVSLoginCheckService {
    private LPVSPullRequestRepository lpvsPullRequestRepository;
    private LPVSMemberRepository memberRepository;

    public LPVSLoginCheckService(LPVSPullRequestRepository lpvsPullRequestRepository, LPVSMemberRepository memberRepository) {
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.memberRepository = memberRepository;
    }

    public Boolean oauthLoginStatus(Authentication authentication) {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            if (oAuth2User == null) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (NullPointerException e) {
            return Boolean.FALSE;
        }
    }
    public Map<String, Object> getOauthLoginMemberMap(Authentication authentication) {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            return attributes;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void loginVerification(Authentication authentication) {
        Boolean loginStatus = oauthLoginStatus(authentication);

        if (!loginStatus) {
            throw new LoginFailedException("LoginFailedException");
        }
    }

    public LPVSMember getMemberFromMemberMap(Authentication authentication) {
        Map<String, Object> memberMap = getOauthLoginMemberMap(authentication);
        String email = (String) memberMap.get("email");
        String provider = (String) memberMap.get("provider");

        LPVSMember findMember = memberRepository.findByEmailAndProvider(email, provider).get();

        return findMember;
    }

}
