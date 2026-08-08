package com.bbangbat.member.application

/**
 * 프로필 이미지 저장소(S3) 접근 포트.
 * member-api는 S3 구현을 직접 의존하지 않고, 구현(어댑터)은 app 모듈에 둔다.
 */
interface ProfileImageStoragePort {
    /** 업로드용 presigned URL과 저장될 오브젝트 key를 발급한다. */
    fun generateUpload(contentType: String): ProfileImageUpload

    /** 저장된 key로 공개 조회 URL을 조립한다. */
    fun buildUrl(objectKey: String): String
}

data class ProfileImageUpload(
    val presignedUrl: String,
    val objectKey: String,
)
