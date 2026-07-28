package com.bbangbat.member.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.EMAIL_ALREADY_REGISTERED
import com.bbangbat.common.exception.ErrorCode.MEMBER_NOT_FOUND
import com.bbangbat.common.exception.ErrorCode.SOCIAL_ALREADY_LINKED
import com.bbangbat.member.domain.AgeGroup
import com.bbangbat.member.domain.Gender
import com.bbangbat.member.domain.Member
import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.FavoritePersistenceAdapter
import com.bbangbat.member.repository.MemberPersistenceAdapter
import com.bbangbat.member.repository.SocialPersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MemberService(
    private val memberPersistenceAdapter: MemberPersistenceAdapter,
    private val socialPersistenceAdapter: SocialPersistenceAdapter,
    private val favoritePersistenceAdapter: FavoritePersistenceAdapter,
    private val reviewPort: ReviewPort,
    private val livePort: LivePort,
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
        if (memberPersistenceAdapter.findByEmailOrNull(email) != null) {
            throw BbangbatException(EMAIL_ALREADY_REGISTERED)
        }

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

    /**
     * 이미 가입된 이메일에 다른 소셜 계정을 연동한다. (temp token으로 소셜 로그인 성공이 검증된 상태)
     */
    @Transactional
    fun link(
        email: String,
        provider: SocialType,
        providerId: String,
    ): Member {
        val member =
            memberPersistenceAdapter.findByEmailOrNull(email)
                ?: throw BbangbatException(MEMBER_NOT_FOUND)

        if (socialPersistenceAdapter.findByProviderAndProviderId(provider, providerId) != null) {
            throw BbangbatException(SOCIAL_ALREADY_LINKED)
        }

        socialPersistenceAdapter.save(Social(member = member, provider = provider, providerId = providerId))
        memberPersistenceAdapter.updateLastLoginAt(member.id)

        return member
    }

    @Transactional
    fun updateLastLoginAt(id: Long) = memberPersistenceAdapter.updateLastLoginAt(id)

    fun findById(id: Long): Member = memberPersistenceAdapter.findById(id)

    fun existsByNickname(nickname: String): Boolean = memberPersistenceAdapter.existsByNickname(nickname)

    fun findByEmailOrNull(email: String): Member? = memberPersistenceAdapter.findByEmailOrNull(email)

    @Transactional(readOnly = true)
    fun getStats(memberId: Long): MemberStats =
        MemberStats(
            reviewCount = reviewPort.countByMemberId(memberId),
            favoriteCount = favoritePersistenceAdapter.countByMemberId(memberId),
            talkCount = livePort.countByMemberId(memberId),
        )
}
