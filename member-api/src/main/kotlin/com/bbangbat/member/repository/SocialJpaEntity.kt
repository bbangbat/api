package com.bbangbat.member.repository

import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode.NO_CONSTRAINT
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "social",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "provider_id"])],
)
@EntityListeners(AuditingEntityListener::class)
class SocialJpaEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false, unique = true)
    var id: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(NO_CONSTRAINT))
    var member: MemberJpaEntity,
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    var provider: SocialType,
    @Column(name = "provider_id", nullable = false, length = 100)
    var providerId: String,
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
) {
    fun toDomain(): Social =
        Social(
            id = id,
            member = member.toDomain(),
            provider = provider,
            providerId = providerId,
        )

    companion object {
        fun from(
            social: Social,
            memberRef: MemberJpaEntity,
        ): SocialJpaEntity =
            SocialJpaEntity(
                id = social.id,
                member = memberRef,
                provider = social.provider,
                providerId = social.providerId,
            )
    }
}
