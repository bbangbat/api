package com.bbangbat.member.repository

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.MEMBER_NOT_FOUND
import com.bbangbat.member.domain.Member
import org.springframework.stereotype.Repository

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

    fun findAllByIds(ids: Collection<Long>): List<Member> = memberRepository.findAllById(ids).map { it.toDomain() }

    fun save(member: Member): Member = memberRepository.save(MemberJpaEntity.from(member)).toDomain()

    fun existsByNickname(nickname: String): Boolean = memberRepository.existsByNickname(nickname)

    /**
     * 변경된 도메인 상태를 영속 엔티티에 반영한다. (더티체킹)
     * 무엇을 어떻게 바꿀지는 도메인이 결정하고, 여기서는 그 결과를 옮겨 담기만 한다.
     */
    fun update(member: Member): Member =
        memberRepository
            .findById(member.id)
            .orElseThrow { BbangbatException(MEMBER_NOT_FOUND) }
            .also { it.applyFrom(member) }
            .toDomain()

    fun deleteById(id: Long) = memberRepository.deleteById(id)
}
