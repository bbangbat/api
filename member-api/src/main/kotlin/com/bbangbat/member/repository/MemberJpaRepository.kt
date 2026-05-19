package com.bbangbat.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.Optional

interface MemberJpaRepository : JpaRepository<MemberJpaEntity, Long> {
    fun findByEmail(email: String): Optional<MemberJpaEntity>

    @Modifying
    @Query("UPDATE MemberJpaEntity m SET m.lastLoginAt = :now WHERE m.id = :id")
    fun updateLastLoginAt(id: Long, now: LocalDateTime)
}
