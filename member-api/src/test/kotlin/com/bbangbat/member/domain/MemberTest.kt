package com.bbangbat.member.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MemberTest {
    @Test
    fun `유효한 회원을 생성할 수 있다`() {
        // when
        val member = Member(email = "test@test.com", name = "홍길동")

        // then
        assertThat(member.email).isEqualTo("test@test.com")
        assertThat(member.name).isEqualTo("홍길동")
    }

    @Test
    fun `이메일은 100자까지 허용된다`() {
        // given
        val email = "a".repeat(91) + "@test.com" // 정확히 100자

        // when
        val member = Member(email = email, name = "홍길동")

        // then
        assertThat(member.email.length).isEqualTo(100)
    }

    @Test
    fun `이메일이 100자를 초과하면 예외가 발생한다`() {
        // given
        val email = "a".repeat(92) + "@test.com" // 101자

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = email, name = "홍길동")
        }
    }

    @Test
    fun `이름은 30자까지 허용된다`() {
        // given
        val name = "가".repeat(30)

        // when
        val member = Member(email = "test@test.com", name = name)

        // then
        assertThat(member.name.length).isEqualTo(30)
    }

    @Test
    fun `이름이 30자를 초과하면 예외가 발생한다`() {
        // given
        val name = "가".repeat(31)

        // when & then
        assertThrows<IllegalArgumentException> {
            Member(email = "test@test.com", name = name)
        }
    }
}
