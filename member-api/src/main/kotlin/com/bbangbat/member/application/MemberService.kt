package com.bbangbat.member.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.EMAIL_ALREADY_REGISTERED
import com.bbangbat.common.exception.ErrorCode.MEMBER_NOT_FOUND
import com.bbangbat.common.exception.ErrorCode.NICKNAME_ALREADY_EXISTS
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
    private val profileImageStoragePort: ProfileImageStoragePort,
) {
    /**
     * 닉네임·프로필 이미지를 수정한다. null인 필드는 변경하지 않는다.
     */
    @Transactional
    fun updateProfile(
        memberId: Long,
        nickname: String?,
        profileImageKey: String?,
    ): Member {
        val current = memberPersistenceAdapter.findById(memberId)

        if (nickname != null && nickname != current.nickname && memberPersistenceAdapter.existsByNickname(nickname)) {
            throw BbangbatException(NICKNAME_ALREADY_EXISTS)
        }

        // 도메인 규칙(닉네임 길이·키 형식) 검증
        current.copy(nickname = nickname ?: current.nickname, profileImageKey = profileImageKey ?: current.profileImageKey)

        return memberPersistenceAdapter.updateProfile(memberId, nickname, profileImageKey)
    }

    /** 프로필 이미지 업로드용 presigned URL 발급 */
    fun generateProfileImageUpload(contentType: String): ProfileImageUpload = profileImageStoragePort.generateUpload(contentType)

    /** 저장된 key를 공개 조회 URL로 변환 */
    fun profileImageUrlOf(member: Member): String? = member.profileImageKey?.let { profileImageStoragePort.buildUrl(it) }

    @Transactional
    fun signup(
        email: String,
        name: String,
        nickname: String,
        profileImageKey: String?,
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
                    profileImageKey = profileImageKey,
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
