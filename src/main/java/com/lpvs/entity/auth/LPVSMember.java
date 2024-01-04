/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.entity.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import jakarta.persistence.*;

/**
 * Represents a member in the LPVS system.
 * This class is mapped to the "member" table in the "lpvs" schema.
 */
@Getter
@NoArgsConstructor
@DynamicUpdate
@Entity
@Table(
        name = "member",
        schema = "lpvs",
        indexes = {@Index(name = "unq_member", columnList = "email, provider", unique = true)})
public class LPVSMember {

    /**
     * The unique identifier for the member.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    /**
     * The name of the member.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * The email address of the member.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * The provider associated with the member.
     */
    @Column(name = "provider", nullable = false)
    private String provider;

    /**
     * The nickname of the member.
     */
    @Column(name = "nickname", nullable = true, unique = true)
    private String nickname;

    /**
     * The organization associated with the member.
     */
    @Column(name = "organization", nullable = true)
    private String organization;

    /**
     * Constructs instances of LPVSMember using the Builder pattern.
     *
     * @param id       The identifier for the LPVSMember.
     * @param name     The name associated with the LPVSMember.
     * @param email    The email address of the LPVSMember.
     * @param provider The provider associated with the LPVSMember.
     * @param nickname The nickname of the LPVSMember.
     */
    @Builder
    public LPVSMember(Long id, String name, String email, String provider, String nickname) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.provider = provider;
        this.nickname = nickname;
    }

    /**
     * Updates the name and email of the member.
     *
     * @param name  The new name of the member.
     * @param email The new email address of the member.
     * @return The updated LPVSMember instance.
     */
    public LPVSMember update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }

    /**
     * Sets the nickname of the member.
     *
     * @param nickname The new nickname of the member.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Sets the organization of the member.
     *
     * @param organization The new organization of the member.
     */
    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
