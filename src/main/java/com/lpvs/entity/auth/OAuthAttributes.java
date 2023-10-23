/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.entity.auth;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public enum OAuthAttributes {
    GOOGLE("google", (attributes) -> {
        MemberProfile memberProfile = new MemberProfile();
        memberProfile.setName((String) attributes.get("name"));
        memberProfile.setEmail((String) attributes.get("email"));
        return memberProfile;
    }),

    NAVER("naver", (attributes) -> {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        System.out.println(response);
        MemberProfile memberProfile = new MemberProfile();
        memberProfile.setName((String) response.get("name"));
        memberProfile.setEmail(((String) response.get("email")));
        return memberProfile;
    }),

    KAKAO("kakao", (attributes) -> {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>)kakaoAccount.get("profile");

        MemberProfile memberProfile = new MemberProfile();
        memberProfile.setName((String) kakaoProfile.get("nickname"));
        memberProfile.setEmail((String) kakaoAccount.get("email"));
        return memberProfile;
    }),

    GITHUB("github", (attributes) -> {
        MemberProfile memberProfile = new MemberProfile();
        memberProfile.setName((String) attributes.get("name"));
        // TODO: The email from Github can be null, so place the login value for a while.
        //       Changing unique key from the member table is required.
        memberProfile.setEmail((String) attributes.get("login"));
        return memberProfile;
    });

    private final String registrationId;
    private final Function<Map<String, Object>, MemberProfile> of;

    OAuthAttributes(String registrationId, Function<Map<String, Object>, MemberProfile> of) {
        this.registrationId = registrationId;
        this.of = of;
    }

    public static MemberProfile 
    extract(String registrationId, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(provider -> registrationId.equals(provider.registrationId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .of.apply(attributes);
    }

}
