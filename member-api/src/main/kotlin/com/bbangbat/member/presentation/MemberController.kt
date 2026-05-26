package com.bbangbat.member.presentation

import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.token.RefreshTokenCookieProvider
import com.bbangbat.auth.token.TempTokenProvider
import com.bbangbat.auth.token.TokenService
import com.bbangbat.member.application.MemberService
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.presentation.dto.MemberResponse
import com.bbangbat.member.presentation.dto.SignupRequest
import com.bbangbat.member.presentation.dto.SignupResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원", description = "회원 API")
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val tempTokenProvider: TempTokenProvider,
    private val jwtProvider: JwtProvider,
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
) {
    @Operation(summary = "소셜 회원가입")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "가입 성공"),
        ApiResponse(responseCode = "401", description = "임시 토큰 유효하지 않음"),
    )
    @PostMapping("/signup")
    @ResponseStatus(CREATED)
    fun signup(
        @RequestBody request: SignupRequest,
        response: HttpServletResponse,
    ): SignupResponse {
        val claims = tempTokenProvider.parse(request.tempToken)
        val member =
            memberService.signup(
                email = claims.email,
                name = claims.name,
                nickname = request.nickname,
                profileImageUrl = request.profileImageUrl,
                gender = request.gender,
                ageGroup = request.ageGroup,
                termsAgreed = request.termsAgreed,
                privacyAgreed = request.privacyAgreed,
                provider = SocialType.valueOf(claims.provider.name),
                providerId = claims.providerId,
            )
        val accessToken = jwtProvider.createAccessToken(member.id)
        val refreshToken = jwtProvider.createRefreshToken(member.id)

        tokenService.saveRefreshToken(member.id, refreshToken)
        refreshTokenCookieProvider.addCookie(response, refreshToken)

        return SignupResponse(accessToken)
    }

    @Operation(summary = "내 정보 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "회원 없음"),
    )
    @GetMapping("/me")
    @ResponseStatus(OK)
    fun getMe(
        @AuthenticationPrincipal memberId: Long,
    ): MemberResponse = MemberResponse.from(memberService.findById(memberId))
}
