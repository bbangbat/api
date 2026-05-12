package com.bbangbat.member.application

import com.bbangbat.member.domain.Member
import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.MemberRepository
import com.bbangbat.member.repository.SocialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val socialRepository: SocialRepository,
) {
    fun findById(id: Long): Member = memberRepository.findById(id)

    fun findByEmailOrNull(email: String): Member? = memberRepository.findByEmailOrNull(email)

    @Transactional
    fun signup(
        email: String,
        name: String,
        nickname: String,
        profileImageUrl: String?,
        provider: SocialType,
        providerId: String,
    ): Member {
        val member =
            memberRepository.save(
                Member(email = email, name = name, nickname = nickname, profileImageUrl = profileImageUrl),
            )

        socialRepository.save(Social(member = member, provider = provider, providerId = providerId))

        return member
    }
}
