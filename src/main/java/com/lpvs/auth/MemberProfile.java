package com.lpvs.auth;

import com.lpvs.entity.LPVSMember;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberProfile {
    private String name;
    private String email;
    private String provider;
    private String nickname;


    public LPVSMember toMember() {
        return LPVSMember.builder()
                .name(name)
                .email(email)
                .provider(provider)
                .build();
    }
}
