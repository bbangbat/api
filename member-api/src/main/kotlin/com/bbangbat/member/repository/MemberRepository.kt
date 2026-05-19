package com.bbangbat.member.repository

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.MEMBER_NOT_FOUND
import com.bbangbat.member.domain.Member
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MemberRepository(
    private val memberJpaRepository: MemberJpaRepository,
) {
    fun findById(id: Long): Member =
        memberJpaRepository
            .findById(id)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .toDomain()

    fun findByEmail(email: String): Member =
        memberJpaRepository
            .findByEmail(email)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .toDomain()

    fun findByEmailOrNull(email: String): Member? =
        memberJpaRepository
            .findByEmail(email)
            .orElse(null)
            ?.toDomain()

    fun save(member: Member): Member = memberJpaRepository.save(MemberJpaEntity.from(member)).toDomain()

    fun updateLastLoginAt(id: Long) = memberJpaRepository.updateLastLoginAt(id, LocalDateTime.now())
}
