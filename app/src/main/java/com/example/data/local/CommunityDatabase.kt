package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Announcement
import com.example.data.model.Message
import com.example.data.model.User

@Database(
    entities = [User::class, Announcement::class, Message::class],
    version = 3,
    exportSchema = false
)
abstract class CommunityDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: CommunityDatabase? = null

        fun getDatabase(context: Context): CommunityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CommunityDatabase::class.java,
                    "community_hub_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
