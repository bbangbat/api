package com.bbangbat.member.api

import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.resolver.AuthMember
import com.bbangbat.auth.resolver.AuthProvider
import com.bbangbat.auth.token.RefreshTokenCookieProvider
import com.bbangbat.auth.token.TempTokenProvider
import com.bbangbat.auth.token.TokenService
import com.bbangbat.member.api.dto.LinkRequest
import com.bbangbat.member.api.dto.LinkSocialRequest
import com.bbangbat.member.api.dto.LinkedSocialResponse
import com.bbangbat.member.api.dto.MemberResponse
import com.bbangbat.member.api.dto.MemberStatsResponse
import com.bbangbat.member.api.dto.ProfileImageUploadRequest
import com.bbangbat.member.api.dto.ProfileImageUploadResponse
import com.bbangbat.member.api.dto.SignupRequest
import com.bbangbat.member.api.dto.SignupResponse
import com.bbangbat.member.api.dto.UpdateProfileRequest
import com.bbangbat.member.application.MemberService
import com.bbangbat.member.application.MemberStatsService
import com.bbangbat.member.domain.AgeGroup
import com.bbangbat.member.domain.Gender
import com.bbangbat.member.domain.NicknamePolicy
import com.bbangbat.member.domain.SocialType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원", description = "회원 API")
@Validated
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val memberStatsService: MemberStatsService,
    private val tempTokenProvider: TempTokenProvider,
    private val jwtProvider: JwtProvider,
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
) {
    @Operation(summary = "소셜 회원가입")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "가입 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 입력값"),
        ApiResponse(responseCode = "401", description = "임시 토큰 유효하지 않음"),
    )
    @PostMapping("/signup")
    @ResponseStatus(CREATED)
    fun signup(
        @RequestBody @Valid request: SignupRequest,
        response: HttpServletResponse,
    ): SignupResponse {
        val claims = tempTokenProvider.parse(request.tempToken)
        val member =
            memberService.signup(
                email = claims.email,
                name = claims.name,
                nickname = request.nickname,
                profileImageKey = request.profileImageKey,
                gender = request.gender ?: claims.gender?.let { Gender.valueOf(it) },
                ageGroup = request.ageGroup ?: claims.ageGroup?.let { AgeGroup.valueOf(it) },
                termsAgreed = request.termsAgreed,
                privacyAgreed = request.privacyAgreed,
                provider = SocialType.valueOf(claims.provider.name),
                providerId = claims.providerId,
            )
        val accessToken = jwtProvider.createAccessToken(member.id, claims.provider.name)
        val refreshToken = jwtProvider.createRefreshToken(member.id, claims.provider.name)

        tokenService.saveRefreshToken(member.id, refreshToken)
        refreshTokenCookieProvider.addCookie(response, refreshToken)

        return SignupResponse(accessToken)
    }

    @Operation(summary = "소셜 계정 연동", description = "이미 가입된 이메일에 다른 소셜 계정을 연동하고 로그인 처리합니다. 연동할 소셜 로그인 후 발급된 임시 토큰이 필요합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "연동 및 로그인 성공"),
        ApiResponse(responseCode = "401", description = "임시 토큰 유효하지 않음"),
        ApiResponse(responseCode = "404", description = "연동할 회원 없음"),
        ApiResponse(responseCode = "409", description = "이미 연동된 소셜 계정"),
    )
    @PostMapping("/link")
    @ResponseStatus(OK)
    fun link(
        @RequestBody @Valid request: LinkRequest,
        response: HttpServletResponse,
    ): SignupResponse {
        val claims = tempTokenProvider.parse(request.tempToken)
        val member =
            memberService.link(
                email = claims.email,
                provider = SocialType.valueOf(claims.provider.name),
                providerId = claims.providerId,
            )
        val accessToken = jwtProvider.createAccessToken(member.id, claims.provider.name)
        val refreshToken = jwtProvider.createRefreshToken(member.id, claims.provider.name)

        tokenService.saveRefreshToken(member.id, refreshToken)
        refreshTokenCookieProvider.addCookie(response, refreshToken)

        return SignupResponse(accessToken)
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다. 비회원도 가능합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "확인 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임"),
    )
    @GetMapping("/nickname/check")
    @ResponseStatus(OK)
    fun checkNickname(
        @RequestParam
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(
            min = NicknamePolicy.MIN_LENGTH,
            max = NicknamePolicy.MAX_LENGTH,
            message = NicknamePolicy.LENGTH_MESSAGE,
        )
        @Pattern(regexp = NicknamePolicy.REGEX, message = NicknamePolicy.FORMAT_MESSAGE)
        nickname: String,
    ): Map<String, Boolean> {
        val available = !memberService.existsByNickname(nickname)

        return mapOf("available" to available)
    }

    @Operation(summary = "내 정보 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "회원 없음"),
    )
    @GetMapping("/me")
    @ResponseStatus(OK)
    fun getMe(
        @AuthMember memberId: Long,
    ): MemberResponse = memberService.findById(memberId).let { MemberResponse.from(it, memberService.profileImageUrlOf(it)) }

    @Operation(summary = "프로필 수정", description = "닉네임·프로필 이미지를 수정합니다. 전달한 필드만 변경됩니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 입력"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "409", description = "닉네임 중복"),
    )
    @PatchMapping("/me")
    @ResponseStatus(OK)
    fun updateProfile(
        @AuthMember memberId: Long,
        @RequestBody @Valid request: UpdateProfileRequest,
    ): MemberResponse =
        memberService
            .updateProfile(memberId, request.name, request.nickname, request.profileImageKey)
            .let { MemberResponse.from(it, memberService.profileImageUrlOf(it)) }

    @Operation(
        summary = "프로필 이미지 업로드 URL 발급",
        description = "발급된 presignedUrl로 5분 내 PUT 업로드 후, objectKey를 프로필 수정 API에 전달하세요.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "발급 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @PostMapping("/me/profile-image/presigned-url")
    @ResponseStatus(OK)
    fun createProfileImageUploadUrl(
        @AuthMember memberId: Long,
        @RequestBody @Valid request: ProfileImageUploadRequest,
    ): ProfileImageUploadResponse = ProfileImageUploadResponse.from(memberService.generateProfileImageUpload(request.contentType!!))

    @Operation(summary = "연동된 소셜 목록 조회", description = "현재 회원에 연동된 소셜 제공자 목록을 반환합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @GetMapping("/me/socials")
    @ResponseStatus(OK)
    fun getLinkedSocials(
        @AuthMember memberId: Long,
        @AuthProvider currentProvider: String?,
    ): List<LinkedSocialResponse> = memberService.findLinkedProviders(memberId).map { LinkedSocialResponse.from(it, currentProvider) }

    @Operation(
        summary = "소셜 계정 연동",
        description =
            "로그인 상태에서 새 소셜 계정을 연동합니다. " +
                "/oauth2/authorization/{provider}?purpose=link 로 소셜 인증 후 받은 임시 토큰이 필요합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "연동 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요 또는 임시 토큰 유효하지 않음"),
        ApiResponse(responseCode = "409", description = "이미 연동된 소셜 계정이거나 해당 제공자가 이미 연동됨"),
    )
    @PostMapping("/me/socials")
    @ResponseStatus(OK)
    fun linkSocial(
        @AuthMember memberId: Long,
        @RequestBody @Valid request: LinkSocialRequest,
    ): List<LinkedSocialResponse> {
        val claims = tempTokenProvider.parse(request.tempToken)

        memberService.linkSocialToMember(memberId, SocialType.valueOf(claims.provider.name), claims.providerId)

        return memberService.findLinkedProviders(memberId).map { LinkedSocialResponse.from(it, null) }
    }

    @Operation(
        summary = "소셜 연동 해제",
        description =
            "지정한 소셜 계정의 연동을 해제합니다. 마지막 소셜 계정은 해제할 수 없습니다. " +
                "네이버는 해제에 소셜 access token이 필요해, 만료된 경우 409(SOCIAL_REAUTH_REQUIRED)를 반환합니다. " +
                "이때 프론트는 해당 소셜 로그인을 다시 수행한 뒤 재시도하세요.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "해제 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "연동되지 않은 소셜 계정"),
        ApiResponse(responseCode = "409", description = "마지막 소셜 계정이거나 소셜 재인증 필요"),
    )
    @DeleteMapping("/social/{provider}")
    @ResponseStatus(NO_CONTENT)
    fun unlinkSocial(
        @AuthMember memberId: Long,
        @PathVariable provider: SocialType,
    ) {
        memberService.unlinkSocial(memberId, provider)
    }

    @Operation(
        summary = "회원 탈퇴",
        description =
            "회원 정보와 즐겨찾기, 혼잡도 투표를 삭제하고 소셜 연동을 해제합니다. " +
                "작성한 리뷰와 실시간 톡은 서비스 데이터로 남습니다. (카카오만 서버에서 연동 해제 가능)",
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "회원 없음"),
    )
    @DeleteMapping("/me")
    @ResponseStatus(NO_CONTENT)
    fun withdraw(
        @AuthMember memberId: Long,
        response: HttpServletResponse,
    ) {
        memberService.withdraw(memberId)
        refreshTokenCookieProvider.clearCookie(response)
    }

    @Operation(summary = "내 활동 수 조회", description = "로그인한 회원의 리뷰, 즐겨찾기, 톡 작성 수를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @GetMapping("/me/stats")
    @ResponseStatus(OK)
    fun getStats(
        @AuthMember memberId: Long,
    ): MemberStatsResponse = MemberStatsResponse.from(memberStatsService.getStats(memberId))
}
