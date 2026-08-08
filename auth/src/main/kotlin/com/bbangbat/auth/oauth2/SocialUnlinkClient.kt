package com.bbangbat.auth.oauth2

import com.bbangbat.auth.token.SocialTokenService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * 소셜 계정 연동을 해제한다.
 *
 * 두 제공자 모두 사용자 access token으로 해제하며, 토큰은 로그인 시점에 짧게 보관해 둔 것을 쓴다.
 * 만료되어 없으면 프론트가 소셜 로그인을 다시 태워 새 토큰을 받아오게 한 뒤 재시도한다.
 */
@Component
class SocialUnlinkClient(
    private val socialTokenService: SocialTokenService,
    @param:Value("\${spring.security.oauth2.client.registration.naver.client-id:}") private val naverClientId: String,
    @param:Value("\${spring.security.oauth2.client.registration.naver.client-secret:}") private val naverClientSecret: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    /** 해제에 쓸 수 있는 소셜 토큰이 보관돼 있는지. false면 소셜 재인증이 필요하다. */
    fun hasUsableToken(
        provider: SocialProvider,
        providerId: String,
    ): Boolean = socialTokenService.find(provider, providerId) != null

    /**
     * 연동 해제를 시도하고 성공 여부를 반환한다. (실패해도 예외를 던지지 않는다)
     */
    fun unlink(
        provider: SocialProvider,
        providerId: String,
    ): Boolean {
        val accessToken = socialTokenService.find(provider, providerId)

        if (accessToken == null) {
            log.warn("연동 해제용 소셜 토큰이 없어 건너뜁니다. 재인증이 필요합니다. provider={}", provider)

            return false
        }

        val result =
            when (provider) {
                SocialProvider.KAKAO -> unlinkKakao(accessToken)
                SocialProvider.NAVER -> unlinkNaver(accessToken)
            }

        socialTokenService.delete(provider, providerId)

        return result
    }

    private fun unlinkKakao(accessToken: String): Boolean =
        runCatching {
            restClient
                .post()
                .uri(KAKAO_UNLINK_URL)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .toBodilessEntity()
        }.onFailure { e ->
            log.error("카카오 연동 해제 실패", e)
        }.isSuccess

    private fun unlinkNaver(accessToken: String): Boolean =
        runCatching {
            restClient
                .post()
                .uri(
                    "$NAVER_UNLINK_URL?grant_type=delete&client_id=$naverClientId" +
                        "&client_secret=$naverClientSecret&access_token=$accessToken&service_provider=NAVER",
                ).retrieve()
                .toBodilessEntity()
        }.onFailure { e ->
            log.error("네이버 연동 해제 실패", e)
        }.isSuccess

    companion object {
        private const val KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink"
        private const val NAVER_UNLINK_URL = "https://nid.naver.com/oauth2.0/token"
    }
}
