package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey
    val id: String = "announcement_active",
    val text: String = "",
    val isActive: Boolean = true,
    val authorName: String = "Admin",
    val authorRole: String = "অ্যাডমিন",
    val priority: String = "high", // "high", "normal"
    val updatedAt: Long = System.currentTimeMillis()
)
