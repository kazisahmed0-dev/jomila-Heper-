package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AutoReplyRule
import com.example.data.model.CallActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.ReminderItem
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY timeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    // Auto Reply Rules
    @Query("SELECT * FROM auto_reply_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<AutoReplyRule>>

    @Query("SELECT * FROM auto_reply_rules WHERE isEnabled = 1")
    suspend fun getActiveRulesSync(): List<AutoReplyRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutoReplyRule): Long

    @Update
    suspend fun updateRule(rule: AutoReplyRule)

    @Delete
    suspend fun deleteRule(rule: AutoReplyRule)

    // Call Activity Logs
    @Query("SELECT * FROM call_activity_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: CallActivityLog): Long

    @Update
    suspend fun updateCallLog(log: CallActivityLog)

    @Delete
    suspend fun deleteCallLog(log: CallActivityLog)
}
