package com.bbangbat.auth.token

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

/** OAuth 결과를 교환하기 위한 1회용 코드의 종류 */
enum class AuthCodeType { LOGIN, SIGNUP, LINK }

data class AuthCodePayload(
    val type: AuthCodeType,
    val memberId: Long? = null,
    val provider: String? = null,
    val tempToken: String? = null,
    val existingAccount: Boolean = false,
)

/**
 * 액세스 토큰/임시 토큰을 리다이렉트 URL에 직접 싣지 않기 위한 1회용 교환 코드.
 *
 * URL에는 코드만 남고, 코드는 짧게 만료되며 한 번 사용하면 즉시 폐기된다.
 * (브라우저 히스토리, Referer, 서버 액세스 로그에 토큰이 남지 않도록)
 */
@Service
class AuthCodeService(
    private val redisTemplate: StringRedisTemplate,
    @param:Value("\${app.redis.key-prefix:}") private val keyPrefix: String,
) {
    private fun key(code: String): String = "${keyPrefix}AUTHCODE:$code"

    fun issue(payload: AuthCodePayload): String {
        val code = UUID.randomUUID().toString()

        redisTemplate.opsForValue().set(key(code), serialize(payload), CODE_TTL)

        return code
    }

    /** 코드를 소비한다. 조회와 삭제가 원자적으로 처리돼 재사용을 막는다. */
    fun consume(code: String): AuthCodePayload? = redisTemplate.opsForValue().getAndDelete(key(code))?.let { deserialize(it) }

    private fun serialize(payload: AuthCodePayload): String =
        listOf(
            payload.type.name,
            payload.memberId?.toString().orEmpty(),
            payload.provider.orEmpty(),
            payload.tempToken.orEmpty(),
            payload.existingAccount.toString(),
        ).joinToString(DELIMITER)

    private fun deserialize(value: String): AuthCodePayload {
        val parts = value.split(DELIMITER)

        return AuthCodePayload(
            type = AuthCodeType.valueOf(parts[0]),
            memberId = parts[1].takeIf { it.isNotEmpty() }?.toLong(),
            provider = parts[2].takeIf { it.isNotEmpty() },
            tempToken = parts[3].takeIf { it.isNotEmpty() },
            existingAccount = parts[4].toBoolean(),
        )
    }

    companion object {
        /** 리다이렉트 직후 곧바로 교환되므로 짧게 유지한다. */
        private val CODE_TTL = Duration.ofSeconds(60)

        /** JWT(base64url + dot)와 provider 이름에 나타나지 않는 구분자 */
        private const val DELIMITER = "|"
    }
}
