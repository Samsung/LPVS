/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.repository;

import com.lpvs.entity.LPVSMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LPVSMemberRepository extends JpaRepository<LPVSMember, Long> {
    Optional<LPVSMember> findByEmailAndProvider(String email, String provider);

    @Query(value = "select m.nickname from LPVSMember m where m.email= :email")
    String findNicknameByEmail(@Param("email") String Email);
}
