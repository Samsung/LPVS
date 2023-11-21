/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.repository;

import com.lpvs.entity.LPVSMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link LPVSMember} entities.
 * Extends {@link org.springframework.data.jpa.repository.JpaRepository} for basic CRUD operations.
 */
public interface LPVSMemberRepository extends JpaRepository<LPVSMember, Long> {
    /**
     * Find a member by email and provider.
     *
     * @param email    The email address of the member.
     * @param provider The provider associated with the member.
     * @return An {@link Optional} containing the {@link LPVSMember} entity if found, otherwise an empty {@link Optional}.
     */
    Optional<LPVSMember> findByEmailAndProvider(String email, String provider);
}
