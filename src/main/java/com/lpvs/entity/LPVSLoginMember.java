package com.lpvs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class LPVSLoginMember {
    private Boolean isLoggedIn;
    private LPVSMember member;
}
