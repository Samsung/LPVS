/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.enums;

import com.lpvs.entity.auth.MemberProfile;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

/**
 * Enum representing OAuth attributes for various providers.
 * Each constant in this enum corresponds to an OAuth provider, providing a way to extract
 * attributes and create a {@link com.lpvs.entity.auth.MemberProfile}.
 */
@SuppressWarnings("unchecked")
public enum OAuthAttributes {

    /**
     * Represents OAuth attributes for Google authentication.
     */
    GOOGLE(
            "google",
            (attributes) -> {
                MemberProfile memberProfile = new MemberProfile();
                memberProfile.setName((String) attributes.get("name"));
                memberProfile.setEmail((String) attributes.get("email"));
                return memberProfile;
            }),

    /**
     * Represents OAuth attributes for Naver authentication.
     */
    NAVER(
            "naver",
            (attributes) -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                MemberProfile memberProfile = new MemberProfile();
                memberProfile.setName((String) response.get("name"));
                memberProfile.setEmail(((String) response.get("email")));
                return memberProfile;
            }),

    /**
     * Represents OAuth attributes for Kakao authentication.
     */
    KAKAO(
            "kakao",
            (attributes) -> {
                Map<String, Object> kakaoAccount =
                        (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> kakaoProfile =
                        (Map<String, Object>) kakaoAccount.get("profile");

                MemberProfile memberProfile = new MemberProfile();
                memberProfile.setName((String) kakaoProfile.get("nickname"));
                memberProfile.setEmail((String) kakaoAccount.get("email"));
                return memberProfile;
            }),

    /**
     * Represents OAuth attributes for GitHub authentication.
     */
    GITHUB(
            "github",
            (attributes) -> {
                MemberProfile memberProfile = new MemberProfile();
                memberProfile.setName((String) attributes.get("name"));
                // TODO: The email from Github can be null, so place the login value for a while.
                //       Changing unique key from the member table is required.
                memberProfile.setEmail((String) attributes.get("login"));
                return memberProfile;
            });

    /**
     * The registration ID associated with the OAuth provider.
     */
    private final String registrationId;

    /**
     * The function to extract OAuth attributes and create a MemberProfile.
     */
    private final Function<Map<String, Object>, MemberProfile> of;

    /**
     * Constructs an OAuthAttributes with the specified registration ID and extraction function.
     *
     * @param registrationId The registration ID associated with the OAuth provider.
     * @param of             The function to extract OAuth attributes and create a MemberProfile.
     */
    OAuthAttributes(String registrationId, Function<Map<String, Object>, MemberProfile> of) {
        this.registrationId = registrationId;
        this.of = of;
    }

    /**
     * Extracts OAuth attributes based on the registration ID and creates a MemberProfile.
     *
     * @param registrationId The registration ID associated with the OAuth provider.
     * @param attributes     The map of OAuth attributes.
     * @return A MemberProfile created from the extracted OAuth attributes.
     * @throws IllegalArgumentException if no matching OAuthAttributes is found for the given registrationId.
     */
    public static MemberProfile extract(String registrationId, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(provider -> registrationId.equals(provider.registrationId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .of
                .apply(attributes);
    }
}
