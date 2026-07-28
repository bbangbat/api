package com.bbangbat.auth.voter

import com.bbangbat.auth.token.AnonymousTokenProvider
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.UNAUTHORIZED
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class VoterResolver(
    private val anonymousTokenProvider: AnonymousTokenProvider,
) {
    fun resolve(request: HttpServletRequest): Voter {
        val principal = SecurityContextHolder.getContext().authentication?.principal

        if (principal is Long) {
            return Voter(VoterType.MEMBER, principal.toString())
        }

        val anonymousId =
            request.cookies
                ?.firstOrNull { it.name == ANONYMOUS_COOKIE }
                ?.value
                ?.let { anonymousTokenProvider.getAnonymousId(it) }

        return if (anonymousId != null) Voter(VoterType.GUEST, anonymousId) else throw BbangbatException(UNAUTHORIZED)
    }

    companion object {
        const val ANONYMOUS_COOKIE = "anonymous_token"
    }
}
