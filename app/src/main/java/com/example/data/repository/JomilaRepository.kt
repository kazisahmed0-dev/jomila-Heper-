package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.model.AutoReplyRule
import com.example.data.model.CallActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.ReminderItem
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

class JomilaRepository(private val dao: AppDao) {

    // Chat
    val allMessages: Flow<List<ChatMessage>> = dao.getAllMessages()
    suspend fun addMessage(message: ChatMessage): Long = dao.insertMessage(message)
    suspend fun clearChat() = dao.clearChatHistory()

    // Reminders
    val allReminders: Flow<List<ReminderItem>> = dao.getAllReminders()
    suspend fun addReminder(reminder: ReminderItem): Long = dao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderItem) = dao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderItem) = dao.deleteReminder(reminder)
    suspend fun getReminderById(id: Long) = dao.getReminderById(id)

    // Tasks
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    suspend fun addTask(task: TaskItem): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = dao.deleteTask(task)

    // Auto Reply Rules
    val allRules: Flow<List<AutoReplyRule>> = dao.getAllRules()
    suspend fun addRule(rule: AutoReplyRule): Long = dao.insertRule(rule)
    suspend fun updateRule(rule: AutoReplyRule) = dao.updateRule(rule)
    suspend fun deleteRule(rule: AutoReplyRule) = dao.deleteRule(rule)
    suspend fun getActiveRulesSync(): List<AutoReplyRule> = dao.getActiveRulesSync()

    // Call Activity Logs
    val allCallLogs: Flow<List<CallActivityLog>> = dao.getAllCallLogs()
    suspend fun addCallLog(log: CallActivityLog): Long = dao.insertCallLog(log)
    suspend fun updateCallLog(log: CallActivityLog) = dao.updateCallLog(log)
    suspend fun deleteCallLog(log: CallActivityLog) = dao.deleteCallLog(log)
}
