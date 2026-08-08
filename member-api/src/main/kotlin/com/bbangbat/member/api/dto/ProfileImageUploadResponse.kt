package com.bbangbat.member.api.dto

import com.bbangbat.member.application.ProfileImageUpload
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "프로필 이미지 업로드 URL 응답")
data class ProfileImageUploadResponse(
    @field:Schema(description = "이 URL로 PUT 업로드 (5분 내 유효)") val presignedUrl: String,
    @field:Schema(description = "업로드 후 프로필 수정 API에 전달할 key") val objectKey: String,
) {
    companion object {
        fun from(upload: ProfileImageUpload): ProfileImageUploadResponse =
            ProfileImageUploadResponse(
                presignedUrl = upload.presignedUrl,
                objectKey = upload.objectKey,
            )
    }
}
