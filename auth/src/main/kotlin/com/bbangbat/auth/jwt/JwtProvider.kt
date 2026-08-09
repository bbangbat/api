package com.bbangbat.auth.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtProperties: JwtProperties,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
    }

    fun createAccessToken(
        memberId: Long,
        provider: String? = null,
    ): String = createToken(memberId, provider, jwtProperties.accessTokenExpiry)

    fun createRefreshToken(
        memberId: Long,
        provider: String? = null,
    ): String = createToken(memberId, provider, jwtProperties.refreshTokenExpiry)

    /** 이번 로그인에 사용한 소셜 제공자. 예전에 발급된 토큰에는 없을 수 있다. */
    fun getProvider(token: String): String? =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload["provider"] as? String

    fun getMemberId(token: String): Long =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()

    fun validateToken(token: String): Boolean =
        try {
            val claims =
                Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .payload
            claims["tokenType"] == TOKEN_TYPE
        } catch (e: JwtException) {
            false
        }

    private fun createToken(
        memberId: Long,
        provider: String?,
        expiry: Long,
    ): String =
        Jwts
            .builder()
            .subject(memberId.toString())
            .claim("tokenType", TOKEN_TYPE)
            .apply { provider?.let { claim("provider", it) } }
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiry))
            .signWith(key)
            .compact()

    companion object {
        private const val TOKEN_TYPE = "ACCESS"
    }
}
