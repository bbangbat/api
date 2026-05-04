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

    fun createAccessToken(memberId: Long): String = createToken(memberId, jwtProperties.accessTokenExpiry)

    fun createRefreshToken(memberId: Long): String = createToken(memberId, jwtProperties.refreshTokenExpiry)

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
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        }

    private fun createToken(
        memberId: Long,
        expiry: Long,
    ): String =
        Jwts
            .builder()
            .subject(memberId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiry))
            .signWith(key)
            .compact()
}
