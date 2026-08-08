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

    /**
     * 프로필 수정 (더티체킹). profileImageKey는 null이면 기존 값을 유지한다.
     */
    fun updateProfile(
        id: Long,
        name: String?,
        nickname: String?,
        profileImageKey: String?,
    ): Member =
        memberRepository
            .findById(id)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .also { entity ->
                name?.let { entity.name = it }
                nickname?.let { entity.nickname = it }
                profileImageKey?.let { entity.profileImageKey = it }
            }.toDomain()

    fun deleteById(id: Long) = memberRepository.deleteById(id)

    fun updateLastLoginAt(id: Long) {
        memberRepository
            .findById(id)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .also { it.lastLoginAt = LocalDateTime.now() }
    }
}
