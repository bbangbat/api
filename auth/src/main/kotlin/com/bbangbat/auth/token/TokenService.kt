package com.bbangbat.auth.token

import com.bbangbat.auth.jwt.JwtProperties
import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.INVALID_TOKEN
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TokenService(
    private val redisTemplate: StringRedisTemplate,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
) {
    fun saveRefreshToken(
        memberId: Long,
        refreshToken: String,
    ) {
        redisTemplate.opsForValue().set(
            "RT:$memberId",
            refreshToken,
            Duration.ofMillis(jwtProperties.refreshTokenExpiry),
        )
    }

    fun rotateToken(refreshToken: String): Pair<String, String> {
        if (!jwtProvider.validateToken(refreshToken)) throw BbangbatException(INVALID_TOKEN)

        val memberId = jwtProvider.getMemberId(refreshToken)
        val stored = redisTemplate.opsForValue().get("RT:$memberId")

        if (stored != refreshToken) throw BbangbatException(INVALID_TOKEN)

        val newAccessToken = jwtProvider.createAccessToken(memberId)
        val newRefreshToken = jwtProvider.createRefreshToken(memberId)

        saveRefreshToken(memberId, newRefreshToken)

        return newAccessToken to newRefreshToken
    }

    fun deleteRefreshToken(memberId: Long) {
        redisTemplate.delete("RT:$memberId")
    }
}
