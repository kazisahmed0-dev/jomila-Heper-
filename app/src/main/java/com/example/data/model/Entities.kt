package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String = ACTION_NONE,
    val actionPayload: String = "" // JSON or delimited info
) {
    companion object {
        const val ACTION_NONE = "NONE"
        const val ACTION_REMINDER = "REMINDER"
        const val ACTION_TASK = "TASK"
        const val ACTION_WHATSAPP = "WHATSAPP"
        const val ACTION_CALL = "CALL"
    }
}

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timeMillis: Long,
    val timeDisplay: String,
    val category: String = "দৈনন্দিন",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "মাঝারি", // উচ্চ, মাঝারি, সাধারণ
    val category: String = "কাজ",
    val aiSubtasks: String = "", // AI generated steps
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "auto_reply_rules")
data class AutoReplyRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val triggerPattern: String, // e.g. "any", "urgent", "kal", "school"
    val replyText: String,
    val isEnabled: Boolean = true,
    val isAiGenerated: Boolean = false,
    val targetApp: String = "WHATSAPP"
)

@Entity(tableName = "call_activity_logs")
data class CallActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val callerName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "MISSED", // INCOMING, MISSED
    val responded: Boolean = false,
    val responseNote: String = ""
)
