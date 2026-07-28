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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {
    @Mock
    private lateinit var memberPersistenceAdapter: MemberPersistenceAdapter

    @Mock
    private lateinit var socialPersistenceAdapter: SocialPersistenceAdapter

    @Mock
    private lateinit var favoritePersistenceAdapter: FavoritePersistenceAdapter

    @Mock
    private lateinit var reviewPort: ReviewPort

    @Mock
    private lateinit var livePort: LivePort

    private lateinit var memberService: MemberService

    @BeforeEach
    fun setUp() {
        memberService =
            MemberService(
                memberPersistenceAdapter,
                socialPersistenceAdapter,
                favoritePersistenceAdapter,
                reviewPort,
                livePort,
            )
    }

    @Test
    fun `회원가입 시 회원과 소셜 계정을 저장한다`() {
        // given
        val savedMember =
            Member(
                id = 1L,
                email = "test@test.com",
                name = "홍길동",
                nickname = "빵괴물",
                profileImageUrl = null,
                gender = Gender.MALE,
                ageGroup = AgeGroup.TWENTIES,
                termsAgreed = true,
                privacyAgreed = true,
                lastLoginAt = LocalDateTime.now(),
            )
        given(memberPersistenceAdapter.save(any())).willReturn(savedMember)

        // when
        val result =
            memberService.signup(
                email = "test@test.com",
                name = "홍길동",
                nickname = "빵괴물",
                profileImageUrl = null,
                gender = Gender.MALE,
                ageGroup = AgeGroup.TWENTIES,
                termsAgreed = true,
                privacyAgreed = true,
                provider = SocialType.KAKAO,
                providerId = "kakao-123",
            )

        // then
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.email).isEqualTo("test@test.com")
        then(socialPersistenceAdapter).should().save(
            Social(member = savedMember, provider = SocialType.KAKAO, providerId = "kakao-123"),
        )
    }

    @Test
    fun `회원가입 시 lastLoginAt이 설정된다`() {
        // given
        val beforeSignup = LocalDateTime.now()
        val savedMember =
            Member(
                id = 1L,
                email = "test@test.com",
                name = "홍길동",
                nickname = "빵괴물",
                profileImageUrl = null,
                gender = Gender.MALE,
                ageGroup = AgeGroup.TWENTIES,
                termsAgreed = true,
                privacyAgreed = true,
                lastLoginAt = LocalDateTime.now(),
            )
        given(memberPersistenceAdapter.save(any())).willReturn(savedMember)
        val memberCaptor = argumentCaptor<Member>()

        // when
        memberService.signup(
            email = "test@test.com",
            name = "홍길동",
            nickname = "빵괴물",
            profileImageUrl = null,
            gender = Gender.MALE,
            ageGroup = AgeGroup.TWENTIES,
            termsAgreed = true,
            privacyAgreed = true,
            provider = SocialType.NAVER,
            providerId = "naver-456",
        )

        // then
        verify(memberPersistenceAdapter).save(memberCaptor.capture())
        assertThat(memberCaptor.firstValue.lastLoginAt).isNotNull().isAfterOrEqualTo(beforeSignup)
    }

    @Test
    fun `사용 중인 닉네임이면 true를 반환한다`() {
        // given
        given(memberPersistenceAdapter.existsByNickname("빵괴물")).willReturn(true)

        // when
        val result = memberService.existsByNickname("빵괴물")

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `사용 가능한 닉네임이면 false를 반환한다`() {
        // given
        given(memberPersistenceAdapter.existsByNickname("새닉네임")).willReturn(false)

        // when
        val result = memberService.existsByNickname("새닉네임")

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `이미 가입된 이메일이면 예외를 던지고 저장하지 않는다`() {
        // given (같은 이메일로 다른 소셜 가입 시도)
        val existing =
            Member(
                id = 1L,
                email = "test@test.com",
                name = "기존회원",
                nickname = "기존닉네임",
                profileImageUrl = null,
                gender = Gender.MALE,
                ageGroup = AgeGroup.TWENTIES,
                termsAgreed = true,
                privacyAgreed = true,
                lastLoginAt = LocalDateTime.now(),
            )
        given(memberPersistenceAdapter.findByEmailOrNull("test@test.com")).willReturn(existing)

        // when & then
        val exception =
            assertThrows<BbangbatException> {
                memberService.signup(
                    email = "test@test.com",
                    name = "홍길동",
                    nickname = "빵괴물",
                    profileImageUrl = null,
                    gender = Gender.MALE,
                    ageGroup = AgeGroup.TWENTIES,
                    termsAgreed = true,
                    privacyAgreed = true,
                    provider = SocialType.NAVER,
                    providerId = "naver-999",
                )
            }

        assertThat(exception.errorCode).isEqualTo(EMAIL_ALREADY_REGISTERED)
        then(memberPersistenceAdapter).should(never()).save(any())
        then(socialPersistenceAdapter).should(never()).save(any())
    }

    @Test
    fun `연동 시 기존 회원에 소셜 계정을 추가한다`() {
        // given
        val existing =
            Member(
                id = 1L,
                email = "test@test.com",
                name = "기존회원",
                nickname = "기존닉네임",
                profileImageUrl = null,
                gender = Gender.MALE,
                ageGroup = AgeGroup.TWENTIES,
                termsAgreed = true,
                privacyAgreed = true,
                lastLoginAt = LocalDateTime.now(),
            )
        given(memberPersistenceAdapter.findByEmailOrNull("test@test.com")).willReturn(existing)
        given(socialPersistenceAdapter.findByProviderAndProviderId(SocialType.NAVER, "naver-123")).willReturn(null)

        // when
        val result = memberService.link("test@test.com", SocialType.NAVER, "naver-123")

        // then
        assertThat(result.id).isEqualTo(1L)
        then(socialPersistenceAdapter).should().save(
            Social(member = existing, provider = SocialType.NAVER, providerId = "naver-123"),
        )
        then(memberPersistenceAdapter).should().updateLastLoginAt(1L)
    }

    @Test
    fun `연동할 회원이 없으면 예외를 던진다`() {
        // given
        given(memberPersistenceAdapter.findByEmailOrNull("none@test.com")).willReturn(null)

        // when & then
        val exception =
            assertThrows<BbangbatException> {
                memberService.link("none@test.com", SocialType.NAVER, "naver-123")
            }

        assertThat(exception.errorCode).isEqualTo(MEMBER_NOT_FOUND)
        then(socialPersistenceAdapter).should(never()).save(any())
    }

    @Test
    fun `이미 연동된 소셜 계정이면 예외를 던진다`() {
        // given
        val existing =
            Member(
                id = 1L,
                email = "test@test.com",
                name = "기존회원",
                nickname = "기존닉네임",
                profileImageUrl = null,
                gender = Gender.MALE,
                ageGroup = AgeGroup.TWENTIES,
                termsAgreed = true,
                privacyAgreed = true,
                lastLoginAt = LocalDateTime.now(),
            )
        val alreadyLinked = Social(id = 5L, member = existing, provider = SocialType.NAVER, providerId = "naver-123")
        given(memberPersistenceAdapter.findByEmailOrNull("test@test.com")).willReturn(existing)
        given(socialPersistenceAdapter.findByProviderAndProviderId(SocialType.NAVER, "naver-123")).willReturn(alreadyLinked)

        // when & then
        val exception =
            assertThrows<BbangbatException> {
                memberService.link("test@test.com", SocialType.NAVER, "naver-123")
            }

        assertThat(exception.errorCode).isEqualTo(SOCIAL_ALREADY_LINKED)
        then(socialPersistenceAdapter).should(never()).save(any())
    }

    @Test
    fun `회원의 리뷰 즐겨찾기 톡 수를 집계한다`() {
        val memberId = 1L
        given(reviewPort.countByMemberId(memberId)).willReturn(3L)
        given(favoritePersistenceAdapter.countByMemberId(memberId)).willReturn(5L)
        given(livePort.countByMemberId(memberId)).willReturn(7L)

        val result = memberService.getStats(memberId)

        assertThat(result.reviewCount).isEqualTo(3L)
        assertThat(result.favoriteCount).isEqualTo(5L)
        assertThat(result.talkCount).isEqualTo(7L)
    }
}
