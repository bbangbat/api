package com.bbangbat.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<MemberJpaEntity, Long> {
    fun findByEmail(email: String): Optional<MemberJpaEntity>

    fun existsByNickname(nickname: String): Boolean
}
