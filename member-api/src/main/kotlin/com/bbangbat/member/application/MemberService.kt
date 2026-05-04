package com.bbangbat.member.application

import com.bbangbat.member.domain.Member
import com.bbangbat.member.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun findById(id: Long): Member = memberRepository.findById(id)

    fun findByEmail(email: String): Member = memberRepository.findByEmail(email)

    fun findByEmailOrNull(email: String): Member? = memberRepository.findByEmailOrNull(email)

    fun save(member: Member): Member = memberRepository.save(member)
}
