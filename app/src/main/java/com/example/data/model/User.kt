package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val district: String = "",
    val thana: String = "",
    val fatherName: String = "",
    val status: String = STATUS_PENDING, // "pending", "approved", "rejected"
    val role: String = ROLE_MEMBER,       // "member", "leader", "president", "admin"
    val avatarUrl: String = "",
    val email: String = "",
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_REJECTED = "rejected"

        const val ROLE_MEMBER = "member"
        const val ROLE_LEADER = "leader"
        const val ROLE_PRESIDENT = "president"
        const val ROLE_ADMIN = "admin"
    }

    val isApproved: Boolean
        get() = status == STATUS_APPROVED

    val isPending: Boolean
        get() = status == STATUS_PENDING

    val isRejected: Boolean
        get() = status == STATUS_REJECTED

    val canPostInImportantGroup: Boolean
        get() = isApproved && (role == ROLE_ADMIN || role == ROLE_PRESIDENT || role == ROLE_LEADER)

    val roleBadge: String
        get() = when (role) {
            ROLE_ADMIN -> "অ্যাডমিন (Admin)"
            ROLE_PRESIDENT -> "সভাপতি (President)"
            ROLE_LEADER -> "নেতৃবৃন্দ (Leader)"
            else -> "সাধারণ সদস্য (Member)"
        }
}
