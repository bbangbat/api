package com.bbangbat.auth.token

import com.bbangbat.auth.jwt.JwtProperties
import com.bbangbat.auth.oauth2.SocialProvider
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.INVALID_TOKEN
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class TempTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret))
    }

    fun createTempToken(
        email: String,
        name: String,
        provider: SocialProvider,
        providerId: String,
        ageGroup: String?,
        gender: String?,
    ): String =
        Jwts
            .builder()
            .subject(email)
            .claim("tokenType", TOKEN_TYPE)
            .claim("name", name)
            .claim("provider", provider.name)
            .claim("providerId", providerId)
            .apply {
                ageGroup?.let { claim("ageGroup", it) }
                gender?.let { claim("gender", it) }
            }.issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + EXPIRY))
            .signWith(key)
            .compact()

    fun parse(token: String): TempTokenClaims =
        try {
            val payload =
                Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .payload

            if (payload["tokenType"] != TOKEN_TYPE) throw BbangbatException(INVALID_TOKEN)

            TempTokenClaims(
                email = payload.subject,
                name = payload["name"] as String,
                provider = SocialProvider.valueOf(payload["provider"] as String),
                providerId = payload["providerId"] as String,
                ageGroup = payload["ageGroup"] as? String,
                gender = payload["gender"] as? String,
            )
        } catch (e: JwtException) {
            throw BbangbatException(INVALID_TOKEN)
        }

    companion object {
        private const val TOKEN_TYPE = "TEMP"
        private const val EXPIRY = 600_000L
    }
}

data class TempTokenClaims(
    val email: String,
    val name: String,
    val provider: SocialProvider,
    val providerId: String,
    val ageGroup: String?,
    val gender: String?,
)
