/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSMember;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.history.HistoryPageEntity;
import com.lpvs.exception.LoginFailedException;
import com.lpvs.exception.WrongAccessException;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class LPVSLoginCheckService {
    private LPVSPullRequestRepository lpvsPullRequestRepository;
    private LPVSMemberRepository memberRepository;

    public LPVSLoginCheckService(LPVSPullRequestRepository lpvsPullRequestRepository, LPVSMemberRepository memberRepository) {
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.memberRepository = memberRepository;
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
        Map<String, Object> oauthLoginMemberMap = getOauthLoginMemberMap(authentication);

        if (oauthLoginMemberMap == null || oauthLoginMemberMap.isEmpty()) {
            throw new LoginFailedException("LoginFailedException");
        }
    }

    @Transactional
    public LPVSMember getMemberFromMemberMap(Authentication authentication) {
        Map<String, Object> memberMap = getOauthLoginMemberMap(authentication);
        String name = (String) memberMap.get("name");
        String email = (String) memberMap.get("email");
        String provider = (String) memberMap.get("provider");

        Optional<LPVSMember> findMemberOptional = memberRepository.findByEmailAndProvider(email, provider);

        if (findMemberOptional.isPresent()) {
            return findMemberOptional.get();
        } else {
            LPVSMember newMember = new LPVSMember();
            newMember.setJoin(name, email, provider);
            memberRepository.save(newMember);
            return newMember;
        }
    }
    public HistoryPageEntity pathCheck(String type, String name,
                                       Pageable pageable, Authentication authentication) {

        loginVerification(authentication);
        LPVSMember findMember = getMemberFromMemberMap(authentication);
        String findNickName = findMember.getNickname();
        String findOrganization = findMember.getOrganization();
        Page<LPVSPullRequest> prPage;
        Long count;

        if ((type.equals("own") && findNickName.equals(name)) ||
                (type.equals("org") && findOrganization.equals(name))) {
            prPage = lpvsPullRequestRepository.findPullRequestByNameLike(name + "/", pageable);
            count = lpvsPullRequestRepository.CountByPullRequestWhereNameLike(name + "/");
        } else if (type.equals("send") && findNickName.equals(name)) {
            prPage = lpvsPullRequestRepository.findBySender(name, pageable);
            count = lpvsPullRequestRepository.CountBySender(name);
        } else {
            throw new WrongAccessException("WrongAccessException");
        }

        return new HistoryPageEntity(prPage, count);
    }

    public String dateTimeFormatting(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
