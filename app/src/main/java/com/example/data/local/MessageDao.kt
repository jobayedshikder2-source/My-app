package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE groupType = :groupType ORDER BY timestamp ASC")
    fun getMessagesForGroup(groupType: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE groupType = :groupType ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessageForGroup(groupType: String): Flow<Message?>

    @Query("SELECT COUNT(*) FROM messages WHERE groupType = :groupType")
    fun getMessageCount(groupType: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Query("UPDATE messages SET reactions = :reactions WHERE id = :messageId")
    suspend fun updateMessageReactions(messageId: String, reactions: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE id LIKE 'msg_gen_%' OR id LIKE 'msg_imp_%'")
    suspend fun deleteExampleMessages()

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
