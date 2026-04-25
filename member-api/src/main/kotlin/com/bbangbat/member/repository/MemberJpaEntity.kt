package com.bbangbat.member.repository

import com.bbangbat.member.domain.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

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
) {
    fun toDomain(): Member =
        Member(
            id = id,
            email = email,
            name = name,
        )

    companion object {
        fun from(member: Member): MemberJpaEntity =
            MemberJpaEntity(
                id = member.id,
                email = member.email,
                name = member.name,
            )
    }
}
