package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String = "",
    val groupType: String = GROUP_GENERAL, // "general" or "important"
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = User.ROLE_MEMBER,
    val text: String = "",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val reactions: String = "" // e.g. "👍,❤️"
) {
    companion object {
        const val GROUP_GENERAL = "general"
        const val GROUP_IMPORTANT = "important"
    }

    val isMediaMessage: Boolean
        get() = !imageUrl.isNullOrEmpty() || !videoUrl.isNullOrEmpty()

    val roleBadge: String
        get() = when (senderRole) {
            User.ROLE_ADMIN -> "অ্যাডমিন"
            User.ROLE_PRESIDENT -> "সভাপতি"
            User.ROLE_LEADER -> "নেতৃবৃন্দ"
            else -> "সদস্য"
        }
}
