package com.bbangbat.auth.token

import com.bbangbat.auth.oauth2.SocialProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 소셜 연동 해제에는 사용자 access token이 필요하다.
 *
 * 평범한 로그인에서는 저장하지 않고, 연동 해제/탈퇴를 위해 재인증한 로그인
 * (purpose=unlink)에서만 아주 짧게 보관한 뒤 사용 즉시 폐기한다.
 */
@Service
class SocialTokenService(
    private val redisTemplate: StringRedisTemplate,
    @param:Value("\${app.redis.key-prefix:}") private val keyPrefix: String,
) {
    private fun key(
        provider: SocialProvider,
        providerId: String,
    ): String = "${keyPrefix}SAT:${provider.name}:$providerId"

    fun save(
        provider: SocialProvider,
        providerId: String,
        accessToken: String,
    ) {
        redisTemplate.opsForValue().set(key(provider, providerId), accessToken, TOKEN_TTL)
    }

    fun find(
        provider: SocialProvider,
        providerId: String,
    ): String? = redisTemplate.opsForValue().get(key(provider, providerId))

    fun delete(
        provider: SocialProvider,
        providerId: String,
    ) {
        redisTemplate.delete(key(provider, providerId))
    }

    companion object {
        /** 재인증 직후 이어지는 해제 요청만 커버하면 되므로 짧게 유지한다. */
        private val TOKEN_TTL = Duration.ofMinutes(5)
    }
}
