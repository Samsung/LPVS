/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.auth.LPVSMember;
import com.lpvs.entity.auth.MemberProfile;
import com.lpvs.entity.enums.OAuthAttributes;
import com.lpvs.repository.LPVSMemberRepository;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for handling OAuth2 user information.
 */
@Service
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * Repository for storing LPVSMember entities.
     */
    private LPVSMemberRepository lpvsMemberRepository;

    /**
     * Default OAuth2 user service.
     */
    @Setter private DefaultOAuth2UserService oAuth2UserService = null;

    /**
     * Constructor for OAuthService.
     *
     * @param lpvsMemberRepository Repository for storing LPVSMember entities.
     */
    @Autowired
    public OAuthService(LPVSMemberRepository lpvsMemberRepository) {
        this.lpvsMemberRepository = lpvsMemberRepository;
    }

    /**
     * Loads user information from the OAuth2 provider.
     *
     * @param userRequest The OAuth2 user request.
     * @return OAuth2User with custom attributes.
     * @throws OAuth2AuthenticationException If an OAuth2 authentication error occurs.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                (null != oAuth2UserService) ? oAuth2UserService : new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName =
                userRequest
                        .getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        MemberProfile memberProfile = OAuthAttributes.extract(registrationId, attributes);
        memberProfile.setProvider(registrationId);
        saveOrUpdate(memberProfile);

        Map<String, Object> customAttribute =
                customAttribute(attributes, userNameAttributeName, memberProfile, registrationId);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("USER")),
                customAttribute,
                userNameAttributeName);
    }

    /**
     * Creates a custom attribute map based on the provided attributes and member profile.
     *
     * @param attributes            OAuth2 user attributes.
     * @param userNameAttributeName The user name attribute name.
     * @param memberProfile         The member profile.
     * @param registrationId        The registration ID.
     * @return Custom attribute map.
     */
    private Map<String, Object> customAttribute(
            Map<String, Object> attributes,
            String userNameAttributeName,
            MemberProfile memberProfile,
            String registrationId) {
        Map<String, Object> customAttribute = new LinkedHashMap<>();
        customAttribute.put(userNameAttributeName, attributes.get(userNameAttributeName));
        customAttribute.put("provider", registrationId);
        customAttribute.put("name", memberProfile.getName());
        customAttribute.put("email", memberProfile.getEmail());
        return customAttribute;
    }

    /**
     * Saves or updates the LPVSMember entity based on the provided member profile.
     *
     * @param memberProfile The member profile to be saved or updated.
     * @return The saved or updated LPVSMember entity.
     */
    private LPVSMember saveOrUpdate(MemberProfile memberProfile) {

        LPVSMember lpvsMember =
                lpvsMemberRepository
                        .findByEmailAndProvider(
                                memberProfile.getEmail(), memberProfile.getProvider())
                        .map(m -> m.update(memberProfile.getName(), memberProfile.getEmail()))
                        .orElse(memberProfile.toMember());

        return lpvsMemberRepository.save(lpvsMember);
    }
}
