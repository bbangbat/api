package com.bbangbat.review.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class S3Service(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.region.static}") private val region: String,
) {
    fun generatePresignedUrl(contentType: String): PresignedUpload {
        val objectKey = "reviews/${UUID.randomUUID()}"

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
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putRequest)
                .build()

        val presigned = s3Presigner.presignPutObject(presignRequest)

        return PresignedUpload(
            presignedUrl = presigned.url().toString(),
            objectKey = objectKey,
        )
    }

    fun buildUrl(objectKey: String): String = "https://$bucket.s3.$region.amazonaws.com/$objectKey"

    fun delete(objectKey: String) {
        s3Client.deleteObject(
            DeleteObjectRequest
                .builder()
                .bucket(bucket)
                .key(objectKey)
                .build(),
        )
    }
}

data class PresignedUpload(
    val presignedUrl: String,
    val objectKey: String,
)
