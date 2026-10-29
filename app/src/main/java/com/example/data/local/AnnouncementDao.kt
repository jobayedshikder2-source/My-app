package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements WHERE isActive = 1 ORDER BY updatedAt DESC LIMIT 1")
    fun getActiveAnnouncement(): Flow<Announcement?>

    @Query("SELECT * FROM announcements ORDER BY updatedAt DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Query("UPDATE announcements SET isActive = 0")
    suspend fun deactivateAllAnnouncements()

    @Query("DELETE FROM announcements WHERE id = 'announcement_seed'")
    suspend fun deleteExampleAnnouncements()

    @Query("DELETE FROM announcements")
    suspend fun deleteAllAnnouncements()
}
