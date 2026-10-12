package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.ai.JomilaBrain
import com.example.data.model.AutoReplyRule
import com.example.data.model.CallActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.ReminderItem
import com.example.data.model.TaskItem
import com.example.data.repository.JomilaRepository
import com.example.util.AlarmScheduler
import com.example.util.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class JomilaViewModel(
    private val repository: JomilaRepository,
    context: Context
) : ViewModel() {

    private val brain = JomilaBrain(repository)
    private val ttsHelper = TtsHelper(context)

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderItem>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<AutoReplyRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallActivityLog>> = repository.allCallLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _isVoiceOutputEnabled = MutableStateFlow(true)
    val isVoiceOutputEnabled: StateFlow<Boolean> = _isVoiceOutputEnabled.asStateFlow()

    private val _isAutoReplyActive = MutableStateFlow(true)
    val isAutoReplyActive: StateFlow<Boolean> = _isAutoReplyActive.asStateFlow()

    private val _isReceptionistActive = MutableStateFlow(true)
    val isReceptionistActive: StateFlow<Boolean> = _isReceptionistActive.asStateFlow()

    fun toggleVoiceOutput() {
        _isVoiceOutputEnabled.value = !_isVoiceOutputEnabled.value
        if (!_isVoiceOutputEnabled.value) {
            ttsHelper.stop()
        }
    }

    fun toggleAutoReplyMaster() {
        _isAutoReplyActive.value = !_isAutoReplyActive.value
    }

    fun toggleReceptionistMaster() {
        _isReceptionistActive.value = !_isReceptionistActive.value
    }

    fun sendMessage(userText: String, context: Context) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            // 1. Store user message
            repository.addMessage(ChatMessage(text = userText, isUser = true))

            _isAiThinking.value = true
            try {
                // 2. Process with Jomila Brain
                val result = brain.processQuery(userText, context)

                // 3. Store Jomila reply
                repository.addMessage(
                    ChatMessage(
                        text = result.replyText,
                        isUser = false,
                        actionType = result.actionType,
                        actionPayload = result.actionPayload
                    )
                )

                // 4. Voice response if enabled
                if (_isVoiceOutputEnabled.value) {
                    val speechText = result.spokenText
                        .replace("⏰", "")
                        .replace("📋", "")
                        .replace("💬", "")
                        .replace("📞", "")
                    ttsHelper.speak(speechText)
                }
            } catch (e: Exception) {
                repository.addMessage(
                    ChatMessage(
                        text = "দুঃখিত ভাইয়া, একটু সমস্যা হয়েছে। আমি আবার চেষ্টা করছি!",
                        isUser = false
                    )
                )
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    // Reminders
    fun addNewReminder(title: String, hour: Int, minute: Int, category: String, context: Context) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val item = ReminderItem(
                title = title,
                timeMillis = cal.timeInMillis,
                timeDisplay = String.format("%02d:%02d", hour, minute),
                category = category,
                isActive = true
            )
            val id = repository.addReminder(item)
            AlarmScheduler.scheduleReminder(context, id, title, cal.timeInMillis)
        }
    }

    fun toggleReminderActive(reminder: ReminderItem, context: Context) {
        viewModelScope.launch {
            val newState = !reminder.isActive
            repository.updateReminder(reminder.copy(isActive = newState))
            if (newState) {
                // Reschedule if in future
                var targetTime = reminder.timeMillis
                if (targetTime <= System.currentTimeMillis()) {
                    targetTime = System.currentTimeMillis() + 60_000
                }
                AlarmScheduler.scheduleReminder(context, reminder.id, reminder.title, targetTime)
            } else {
                AlarmScheduler.cancelReminder(context, reminder.id)
            }
        }
    }

    fun deleteReminder(reminder: ReminderItem, context: Context) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(context, reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    // Tasks
    fun addNewTask(title: String, notes: String, priority: String, category: String) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title,
                notes = notes,
                priority = priority,
                category = category,
                isCompleted = false
            )
            repository.addTask(task)
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun requestAiHelpForTask(task: TaskItem) {
        viewModelScope.launch {
            val prompt = "Task: ${task.title}. Provide 3 easy, practical action steps in Bengali to complete this task quickly."
            val aiResponse = if (GeminiClient.isApiKeyConfigured()) {
                val res = GeminiClient.generateResponse(prompt)
                res.getOrNull() ?: "১. লক্ষ্য নির্ধারণ করুন\n২. মনোযোগ দিয়ে শুরু করুন\n৩. সম্পন্ন হলে রিভিউ করুন"
            } else {
                "১. প্রয়োজনীয় উপকরণ গুছিয়ে নিন\n২. প্রথম ধাপটি ১০ মিনিটে শুরু করুন\n৩. জমিলাকে সফলতার খবর দিন!"
            }

            repository.updateTask(task.copy(aiSubtasks = aiResponse))
        }
    }

    // Auto-Reply Rules
    fun addNewRule(triggerPattern: String, replyText: String) {
        viewModelScope.launch {
            repository.addRule(
                AutoReplyRule(
                    triggerPattern = triggerPattern,
                    replyText = replyText,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleRule(rule: AutoReplyRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteRule(rule: AutoReplyRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    // Simulate WhatsApp Message incoming
    fun simulateIncomingWhatsApp(sender: String, messageText: String) {
        viewModelScope.launch {
            val rulesList = rules.value.filter { it.isEnabled }
            val matched = rulesList.firstOrNull { rule ->
                if (rule.triggerPattern.equals("any", true)) false
                else messageText.contains(rule.triggerPattern, true)
            } ?: rulesList.firstOrNull { it.triggerPattern.equals("any", true) }

            val reply = matched?.replyText ?: "ধন্যবাদ! জমিলা হেল্পার আপনার মেসেজটি গ্রহণ করেছে।"

            repository.addMessage(
                ChatMessage(
                    text = "📥 [সিমুলেশন: হোয়াটসঅ্যাপ মেসেজ এসেছে]\nপ্রেরক: $sender\nমেসেজ: \"$messageText\"\n\n🤖 জমিলার স্বয়ংক্রিয় উত্তর:\n\"$reply\"",
                    isUser = false,
                    actionType = ChatMessage.ACTION_WHATSAPP,
                    actionPayload = "$sender||||$reply"
                )
            )

            if (_isVoiceOutputEnabled.value) {
                ttsHelper.speak("হোয়াটসঅ্যাপে $sender এর মেসেজের উত্তর পাঠিয়েছি।")
            }
        }
    }

    // Clear chat
    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            repository.addMessage(
                ChatMessage(
                    text = "চ্যাট হিস্ট্রি রিসেট করা হয়েছে। আমি জমিলা, আপনার সেবায় প্রস্তুত!",
                    isUser = false
                )
            )
        }
    }

    fun speakAloud(text: String) {
        ttsHelper.speak(text)
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }

    class Factory(
        private val repository: JomilaRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JomilaViewModel::class.java)) {
                return JomilaViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
