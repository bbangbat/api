package com.bbangbat.member.repository

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.MEMBER_NOT_FOUND
import com.bbangbat.member.domain.Member
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MemberPersistenceAdapter(
    private val memberRepository: MemberRepository,
) {
    fun findById(id: Long): Member =
        memberRepository
            .findById(id)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .toDomain()

    fun findByEmailOrNull(email: String): Member? =
        memberRepository
            .findByEmail(email)
            .orElse(null)
            ?.toDomain()

    fun save(member: Member): Member = memberRepository.save(MemberJpaEntity.from(member)).toDomain()

    fun existsByNickname(nickname: String): Boolean = memberRepository.existsByNickname(nickname)

    fun updateLastLoginAt(id: Long) {
        memberRepository
            .findById(id)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .also { it.lastLoginAt = LocalDateTime.now() }
    }
}
