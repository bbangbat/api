package com.bbangbat.member.application

import com.bbangbat.auth.oauth2.SocialProvider
import com.bbangbat.auth.oauth2.SocialUnlinkClient
import com.bbangbat.auth.token.TokenService
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.CURRENT_SOCIAL_CANNOT_UNLINK
import com.bbangbat.common.exception.ErrorCode.EMAIL_ALREADY_REGISTERED
import com.bbangbat.common.exception.ErrorCode.LAST_SOCIAL_CANNOT_UNLINK
import com.bbangbat.common.exception.ErrorCode.MEMBER_NOT_FOUND
import com.bbangbat.common.exception.ErrorCode.NICKNAME_ALREADY_EXISTS
import com.bbangbat.common.exception.ErrorCode.SOCIAL_ALREADY_LINKED
import com.bbangbat.common.exception.ErrorCode.SOCIAL_NOT_LINKED
import com.bbangbat.common.exception.ErrorCode.SOCIAL_REAUTH_REQUIRED
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
    private val livePort: LivePort,
    private val profileImageStoragePort: ProfileImageStoragePort,
    private val socialUnlinkClient: SocialUnlinkClient,
    private val tokenService: TokenService,
) {
    /**
     * 이름/닉네임/프로필 이미지/성별/연령대를 수정한다. null인 필드는 변경하지 않는다.
     */
    @Transactional
    fun updateProfile(
        memberId: Long,
        name: String?,
        nickname: String?,
        profileImageKey: String?,
        gender: Gender?,
        ageGroup: AgeGroup?,
    ): Member {
        val current = memberPersistenceAdapter.findById(memberId)

        if (nickname != null && nickname != current.nickname && memberPersistenceAdapter.existsByNickname(nickname)) {
            throw BbangbatException(NICKNAME_ALREADY_EXISTS)
        }

        // 도메인 규칙(이름/닉네임 길이, 이미지 키 형식) 검증
        current.copy(
            name = name ?: current.name,
            nickname = nickname ?: current.nickname,
            profileImageKey = profileImageKey ?: current.profileImageKey,
            gender = gender ?: current.gender,
            ageGroup = ageGroup ?: current.ageGroup,
        )

        return memberPersistenceAdapter.updateProfile(memberId, name, nickname, profileImageKey, gender, ageGroup)
    }

    /** 로그인한 회원에게 소셜 계정을 연동한다. (마이페이지 연동, 이메일과 무관하게 현재 회원에 연결) */
    @Transactional
    fun linkSocialToMember(
        memberId: Long,
        provider: SocialType,
        providerId: String,
    ): Member {
        val member = memberPersistenceAdapter.findById(memberId)
        val alreadyLinkedElsewhere = socialPersistenceAdapter.findByProviderAndProviderId(provider, providerId) != null
        val alreadyHasProvider = socialPersistenceAdapter.findAllByMemberId(memberId).any { it.provider == provider }

        if (alreadyLinkedElsewhere || alreadyHasProvider) {
            throw BbangbatException(SOCIAL_ALREADY_LINKED)
        }

        socialPersistenceAdapter.save(Social(member = member, provider = provider, providerId = providerId))

        return member
    }

    /** 연동된 소셜 제공자 목록 */
    @Transactional(readOnly = true)
    fun findLinkedProviders(memberId: Long): List<SocialType> = socialPersistenceAdapter.findAllByMemberId(memberId).map { it.provider }

    /**
     * 회원 탈퇴 (하드 삭제).
     * 소셜 연동을 해제하고 회원/소셜/즐겨찾기/혼잡도 투표와 리프레시 토큰을 제거한다.
     * 작성 콘텐츠(리뷰·실시간 톡)는 서비스 데이터로 남긴다. 톡은 닉네임 스냅샷이라 표시에 문제가 없고,
     * 리뷰는 회원 정보를 조회하지 않아 조회가 깨지지 않는다.
     */
    @Transactional
    fun withdraw(memberId: Long) {
        val member = memberPersistenceAdapter.findById(memberId)
        val socials = socialPersistenceAdapter.findAllByMemberId(member.id)

        // 재인증 없이 해제 가능한 건이 하나도 없으면, 프론트가 소셜 로그인을 다시 태우도록 유도한다.
        if (socials.isNotEmpty() && socials.none { socialUnlinkClient.hasUsableToken(providerOf(it.provider), it.providerId) }) {
            throw BbangbatException(SOCIAL_REAUTH_REQUIRED)
        }

        // 탈퇴는 반드시 완료돼야 하므로 개별 해제 실패는 로그만 남기고 진행한다.
        socials.forEach { social -> socialUnlinkClient.unlink(providerOf(social.provider), social.providerId) }

        livePort.deleteCongestionVotesByMemberId(member.id)
        favoritePersistenceAdapter.deleteAllByMemberId(member.id)
        socialPersistenceAdapter.deleteAllByMemberId(member.id)
        memberPersistenceAdapter.deleteById(member.id)
        tokenService.deleteRefreshToken(member.id)
    }

    /**
     * 소셜 계정 연동 해제 (회원은 유지).
     * 마지막 소셜 계정은 해제할 수 없다. 해제하면 로그인 수단이 사라지기 때문이다.
     * 현재 로그인에 사용 중인 소셜도 해제할 수 없다. 해제하면 지금 세션의 근거가 사라진다.
     * 네이버처럼 access token이 필요한데 보관된 토큰이 없으면 재인증을 요구한다.
     */
    @Transactional
    fun unlinkSocial(
        memberId: Long,
        provider: SocialType,
        currentProvider: String?,
    ) {
        if (provider.name == currentProvider) {
            throw BbangbatException(CURRENT_SOCIAL_CANNOT_UNLINK)
        }

        val socials = socialPersistenceAdapter.findAllByMemberId(memberId)
        val target = socials.firstOrNull { it.provider == provider } ?: throw BbangbatException(SOCIAL_NOT_LINKED)

        if (socials.size <= 1) {
            throw BbangbatException(LAST_SOCIAL_CANNOT_UNLINK)
        }

        val socialProvider = providerOf(provider)

        if (!socialUnlinkClient.hasUsableToken(socialProvider, target.providerId)) {
            throw BbangbatException(SOCIAL_REAUTH_REQUIRED)
        }

        if (!socialUnlinkClient.unlink(socialProvider, target.providerId)) {
            throw BbangbatException(SOCIAL_REAUTH_REQUIRED)
        }

        socialPersistenceAdapter.delete(target.id)
    }

    private fun providerOf(provider: SocialType): SocialProvider = SocialProvider.valueOf(provider.name)

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
        gender: Gender?,
        ageGroup: AgeGroup?,
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
                    gender = gender ?: Gender.UNKNOWN,
                    ageGroup = ageGroup ?: AgeGroup.UNKNOWN,
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

    fun findByIds(ids: Collection<Long>): List<Member> = memberPersistenceAdapter.findAllByIds(ids)

    /** 소셜 계정에 연동된 회원 ID. 연동된 회원이 없으면 null */
    fun findMemberIdBySocial(
        provider: SocialType,
        providerId: String,
    ): Long? = socialPersistenceAdapter.findByProviderAndProviderId(provider, providerId)?.member?.id
}
