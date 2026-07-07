package com.bbangbat.member.application

import com.bbangbat.member.domain.AgeGroup
import com.bbangbat.member.domain.Gender
import com.bbangbat.member.domain.Member
import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.MemberPersistenceAdapter
import com.bbangbat.member.repository.SocialPersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MemberService(
    private val memberPersistenceAdapter: MemberPersistenceAdapter,
    private val socialPersistenceAdapter: SocialPersistenceAdapter,
) {
    @Transactional
    fun signup(
        email: String,
        name: String,
        nickname: String,
        profileImageUrl: String?,
        gender: Gender,
        ageGroup: AgeGroup,
        termsAgreed: Boolean,
        privacyAgreed: Boolean,
        provider: SocialType,
        providerId: String,
    ): Member {
        val member =
            memberPersistenceAdapter.save(
                Member(
                    email = email,
                    name = name,
                    nickname = nickname,
                    profileImageUrl = profileImageUrl,
                    gender = gender,
                    ageGroup = ageGroup,
                    termsAgreed = termsAgreed,
                    privacyAgreed = privacyAgreed,
                    lastLoginAt = LocalDateTime.now(),
                ),
            )

        socialPersistenceAdapter.save(Social(member = member, provider = provider, providerId = providerId))

        return member
    }

    @Transactional
    fun updateLastLoginAt(id: Long) = memberPersistenceAdapter.updateLastLoginAt(id)

    fun findById(id: Long): Member = memberPersistenceAdapter.findById(id)

    fun existsByNickname(nickname: String): Boolean = memberPersistenceAdapter.existsByNickname(nickname)

    fun findByEmailOrNull(email: String): Member? = memberPersistenceAdapter.findByEmailOrNull(email)
}
