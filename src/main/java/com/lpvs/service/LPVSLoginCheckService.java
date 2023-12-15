/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.auth.LPVSMember;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service class for handling login-related operations in LPVS.
 */
@Service
public class LPVSLoginCheckService {

    /**
     * Repository for interacting with LPVS pull requests.
     */
    private LPVSPullRequestRepository lpvsPullRequestRepository;

    /**
     * Repository for interacting with LPVS members.
     */
    private LPVSMemberRepository memberRepository;

    /**
     * Constructor for LPVSLoginCheckService.
     *
     * @param lpvsPullRequestRepository Repository for LPVS pull requests.
     * @param memberRepository         Repository for LPVS members.
     */
    public LPVSLoginCheckService(
            LPVSPullRequestRepository lpvsPullRequestRepository,
            LPVSMemberRepository memberRepository) {
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Get OAuth login member map from the authentication object.
     *
     * @param authentication The authentication object.
     * @return Map representing the attributes of the OAuth login member.
     */
    public Map<String, Object> getOauthLoginMemberMap(Authentication authentication) {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            return attributes;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Perform login verification using the authentication object.
     *
     * @param authentication The authentication object.
     * @throws LoginFailedException If login verification fails.
     */
    public void loginVerification(Authentication authentication) {
        Map<String, Object> oauthLoginMemberMap = getOauthLoginMemberMap(authentication);

        if (oauthLoginMemberMap == null || oauthLoginMemberMap.isEmpty()) {
            throw new LoginFailedException("LoginFailedException");
        }
    }

    /**
     * Get an LPVSMember from the OAuth login member map.
     *
     * @param authentication The authentication object.
     * @return LPVSMember extracted from the OAuth login member map.
     */
    public LPVSMember getMemberFromMemberMap(Authentication authentication) {
        Map<String, Object> memberMap = getOauthLoginMemberMap(authentication);
        String email = (String) memberMap.get("email");
        String provider = (String) memberMap.get("provider");

        LPVSMember findMember = memberRepository.findByEmailAndProvider(email, provider).get();

        return findMember;
    }

    /**
     * Check access based on type, name, pageable, and authentication.
     *
     * @param type           The type of access.
     * @param name           The name associated with access.
     * @param pageable       Pageable object for pagination.
     * @param authentication The authentication object.
     * @return HistoryPageEntity representing the results of access verification.
     * @throws WrongAccessException If access verification fails.
     */
    public HistoryPageEntity pathCheck(
            String type, String name, Pageable pageable, Authentication authentication) {

        loginVerification(authentication);
        LPVSMember findMember = getMemberFromMemberMap(authentication);
        String findNickName = findMember.getNickname();
        String findOrganization = findMember.getOrganization();
        Page<LPVSPullRequest> prPage;
        Long count;

        if ((type.equals("own") && findNickName.equals(name))
                || (type.equals("org") && findOrganization.equals(name))) {
            prPage = lpvsPullRequestRepository.findByPullRequestBase(name, pageable);
            count = lpvsPullRequestRepository.CountByPullRequestBase(name);
        } else if (type.equals("send") && findNickName.equals(name)) {
            prPage = lpvsPullRequestRepository.findBySenderOrPullRequestHead(name, pageable);
            count = lpvsPullRequestRepository.CountBySenderOrPullRequestHead(name);
        } else {
            throw new WrongAccessException("WrongAccessException");
        }

        return new HistoryPageEntity(prPage, count);
    }

    /**
     * Format LocalDateTime object as a string.
     *
     * @param localDateTime The LocalDateTime object to format.
     * @return String representation of the formatted LocalDateTime.
     */
    public String dateTimeFormatting(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
