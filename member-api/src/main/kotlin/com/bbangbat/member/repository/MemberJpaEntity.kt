package com.bbangbat.member.repository

import com.bbangbat.member.domain.AgeGroup
import com.bbangbat.member.domain.Gender
import com.bbangbat.member.domain.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "members")
class MemberJpaEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false, unique = true)
    var id: Long = 0L,
    @Column(name = "email", nullable = false, unique = true, length = 100)
    var email: String,
    @Column(name = "name", nullable = false, length = 30)
    var name: String,
    @Column(name = "nickname", nullable = false, length = 20)
    var nickname: String,
    @Column(name = "profile_image_key", nullable = true, length = 500)
    var profileImageKey: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    var gender: Gender,
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    var ageGroup: AgeGroup,
    @Column(name = "terms_agreed", nullable = false)
    var termsAgreed: Boolean,
    @Column(name = "privacy_agreed", nullable = false)
    var privacyAgreed: Boolean,
    @Column(name = "last_login_at", nullable = true)
    var lastLoginAt: LocalDateTime? = null,
) : BaseEntity() {
    fun toDomain(): Member =
        Member(
            id = id,
            email = email,
            name = name,
            nickname = nickname,
            profileImageKey = profileImageKey,
            gender = gender,
            ageGroup = ageGroup,
            termsAgreed = termsAgreed,
            privacyAgreed = privacyAgreed,
            lastLoginAt = lastLoginAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun from(member: Member): MemberJpaEntity =
            MemberJpaEntity(
                id = member.id,
                email = member.email,
                name = member.name,
                nickname = member.nickname,
                profileImageKey = member.profileImageKey,
                gender = member.gender,
                ageGroup = member.ageGroup,
                termsAgreed = member.termsAgreed,
                privacyAgreed = member.privacyAgreed,
                lastLoginAt = member.lastLoginAt,
            )
    }
}
