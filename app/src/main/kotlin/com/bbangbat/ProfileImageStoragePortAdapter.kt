package com.bbangbat

import com.bbangbat.member.application.ProfileImageStoragePort
import com.bbangbat.member.application.ProfileImageUpload
import com.bbangbat.member.domain.Member
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Component
class ProfileImageStoragePortAdapter(
    private val s3Presigner: S3Presigner,
    @param:Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @param:Value("\${cloud.aws.region.static}") private val region: String,
) : ProfileImageStoragePort {
    override fun generateUpload(contentType: String): ProfileImageUpload {
        val objectKey = "${Member.PROFILE_IMAGE_KEY_PREFIX}${UUID.randomUUID()}"
        val putRequest =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build()
        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(PRESIGN_MINUTES))
                .putObjectRequest(putRequest)
                .build()

        return ProfileImageUpload(
            presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString(),
            objectKey = objectKey,
        )
    }

    override fun buildUrl(objectKey: String): String = "https://$bucket.s3.$region.amazonaws.com/$objectKey"

    companion object {
        private const val PRESIGN_MINUTES = 5L
    }
}
