package com.bbangbat.member.application

import com.bbangbat.member.domain.AgeGroup
import com.bbangbat.member.domain.Gender
import com.bbangbat.member.domain.Member
import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.MemberRepository
import com.bbangbat.member.repository.SocialRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class MemberServiceTest {
    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var socialRepository: SocialRepository

    private lateinit var memberService: MemberService

    @BeforeEach
    fun setUp() {
        memberService = MemberService(memberRepository, socialRepository)
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
        given(memberRepository.save(any())).willReturn(savedMember)

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
        then(socialRepository).should().save(
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
        given(memberRepository.save(any())).willReturn(savedMember)
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
        verify(memberRepository).save(memberCaptor.capture())
        assertThat(memberCaptor.firstValue.lastLoginAt).isNotNull().isAfterOrEqualTo(beforeSignup)
    }

    @Test
    fun `사용 중인 닉네임이면 true를 반환한다`() {
        // given
        given(memberRepository.existsByNickname("빵괴물")).willReturn(true)

        // when
        val result = memberService.existsByNickname("빵괴물")

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `사용 가능한 닉네임이면 false를 반환한다`() {
        // given
        given(memberRepository.existsByNickname("새닉네임")).willReturn(false)

        // when
        val result = memberService.existsByNickname("새닉네임")

        // then
        assertThat(result).isFalse()
    }
}
