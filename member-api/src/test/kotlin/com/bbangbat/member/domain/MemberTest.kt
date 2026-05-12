package com.bbangbat.member.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MemberTest {
    @Test
    fun `유효한 회원을 생성할 수 있다`() {
        // given
        val email = "test@test.com"
        val name = "홍길동"
        val nickname = "빵괴물"

        // when
        val member = Member(email = email, name = name, nickname = nickname)

        // then
        assertThat(member.email).isEqualTo("test@test.com")
        assertThat(member.name).isEqualTo("홍길동")
        assertThat(member.nickname).isEqualTo("빵괴물")
        assertThat(member.profileImageUrl).isNull()
    }

    @Test
    fun `프로필 이미지 URL을 포함한 회원을 생성할 수 있다`() {
        // given
        val profileImageUrl = "https://example.com/image.jpg"

        // when
        val member =
            Member(
                email = "test@test.com",
                name = "홍길동",
                nickname = "빵괴물",
                profileImageUrl = profileImageUrl,
            )

        // then
        assertThat(member.profileImageUrl).isEqualTo("https://example.com/image.jpg")
    }

    @Test
    fun `이메일은 100자까지 허용된다`() {
        // given
        val email = "a".repeat(91) + "@test.com"

        // when
        val member = Member(email = email, name = "홍길동", nickname = "빵괴물")

        // then
        assertThat(member.email.length).isEqualTo(100)
    }

    @Test
    fun `이메일이 비어있으면 예외가 발생한다`() {
        // given
        val email = ""

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = email, name = "홍길동", nickname = "빵괴물")
        }
    }

    @Test
    fun `이메일이 100자를 초과하면 예외가 발생한다`() {
        // given
        val email = "a".repeat(92) + "@test.com"

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = email, name = "홍길동", nickname = "빵괴물")
        }
    }

    @Test
    fun `이름은 30자까지 허용된다`() {
        // given
        val name = "가".repeat(30)

        // when
        val member = Member(email = "test@test.com", name = name, nickname = "빵괴물")

        // then
        assertThat(member.name.length).isEqualTo(30)
    }

    @Test
    fun `이름이 비어있으면 예외가 발생한다`() {
        // given
        val name = ""

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = name, nickname = "빵괴물")
        }
    }

    @Test
    fun `이름이 30자를 초과하면 예외가 발생한다`() {
        // given
        val name = "가".repeat(31)

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = name, nickname = "빵괴물")
        }
    }

    @Test
    fun `닉네임은 2자 이상 20자 이하여야 한다`() {
        // given
        val minNickname = "빵괴"
        val maxNickname = "가".repeat(20)

        // when
        val memberWithMin = Member(email = "test@test.com", name = "홍길동", nickname = minNickname)
        val memberWithMax = Member(email = "test@test.com", name = "홍길동", nickname = maxNickname)

        // then
        assertThat(memberWithMin.nickname.length).isEqualTo(2)
        assertThat(memberWithMax.nickname.length).isEqualTo(20)
    }

    @Test
    fun `닉네임이 비어있으면 예외가 발생한다`() {
        // given
        val nickname = "   "

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = "홍길동", nickname = nickname)
        }
    }

    @Test
    fun `닉네임이 2자 미만이면 예외가 발생한다`() {
        // given
        val nickname = "빵"

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = "홍길동", nickname = nickname)
        }
    }

    @Test
    fun `닉네임이 20자를 초과하면 예외가 발생한다`() {
        // given
        val nickname = "가".repeat(21)

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = "홍길동", nickname = nickname)
        }
    }

    @Test
    fun `프로필 이미지 URL이 https로 시작하지 않으면 예외가 발생한다`() {
        // given
        val profileImageUrl = "http://example.com/image.jpg"

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = "홍길동", nickname = "빵괴물", profileImageUrl = profileImageUrl)
        }
    }

    @Test
    fun `프로필 이미지 URL이 500자를 초과하면 예외가 발생한다`() {
        // given
        val profileImageUrl = "https://" + "a".repeat(493)

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = "홍길동", nickname = "빵괴물", profileImageUrl = profileImageUrl)
        }
    }
}
