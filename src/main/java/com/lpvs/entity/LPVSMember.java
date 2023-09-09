package com.lpvs.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter @NoArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "member")
public class LPVSMember {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "nickname", nullable = true, unique = true)
    private String nickname;

    @Column(name = "organization", nullable = true)
    private String organization;

    @Builder
    public LPVSMember(Long id, String name, String email, String provider, String nickname) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
    }

    public LPVSMember update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
