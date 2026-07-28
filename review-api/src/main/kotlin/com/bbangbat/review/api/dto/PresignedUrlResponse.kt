package com.bbangbat.review.api.dto

import com.bbangbat.review.application.PresignedUpload

data class PresignedUrlResponse(
    val presignedUrl: String,
    val objectKey: String,
) {
    companion object {
        fun from(upload: PresignedUpload) =
            PresignedUrlResponse(
                presignedUrl = upload.presignedUrl,
                objectKey = upload.objectKey,
            )
    }
}
