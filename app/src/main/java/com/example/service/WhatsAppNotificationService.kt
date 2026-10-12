package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.RemoteInput
import com.example.data.db.AppDatabase
import com.example.data.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WhatsAppNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "WhatsAppService"
        var isServiceRunning = false
        var lastRepliedSender: String = ""
        var lastRepliedText: String = ""
        var lastRepliedTime: Long = 0
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceRunning = true
        Log.d(TAG, "Notification Listener connected successfully.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceRunning = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() || text.isBlank()) return
        // Ignore summary or group notifications without individual messages
        if (title.contains("WhatsApp") && text.contains("messages", ignoreCase = true)) return

        // Prevent duplicate spam loops within 5 seconds for same sender
        val now = System.currentTimeMillis()
        if (title == lastRepliedSender && text == lastRepliedText && (now - lastRepliedTime) < 5000) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val activeRules = db.appDao().getActiveRulesSync()
                if (activeRules.isEmpty()) return@launch

                // Match rule: check if any regex matches text, or default "any"
                val matchedRule = activeRules.firstOrNull { rule ->
                    if (rule.triggerPattern.equals("any", ignoreCase = true)) {
                        false // check specific ones first
                    } else {
                        try {
                            Regex(rule.triggerPattern, RegexOption.IGNORE_CASE).containsMatchIn(text)
                        } catch (_: Exception) {
                            text.contains(rule.triggerPattern, ignoreCase = true)
                        }
                    }
                } ?: activeRules.firstOrNull { it.triggerPattern.equals("any", ignoreCase = true) }

                if (matchedRule != null) {
                    val replyMessage = matchedRule.replyText

                    // Attempt remote reply via Notification Action
                    val replied = trySendRemoteReply(applicationContext, notification, replyMessage)

                    lastRepliedSender = title
                    lastRepliedText = text
                    lastRepliedTime = now

                    // Record in Jomila Chat feed as automated assistant action
                    db.appDao().insertMessage(
                        ChatMessage(
                            text = "🤖 [হোয়াটসঅ্যাপ অটো-রিপ্লাই পাঠানো হয়েছে]\nপ্রাপক: $title\nমেসেজ: \"$text\"\nজমিলার উত্তর: \"$replyMessage\"",
                            isUser = false,
                            actionType = ChatMessage.ACTION_WHATSAPP,
                            actionPayload = "$title||$replyMessage"
                        )
                    )

                    Log.d(TAG, "Sent WhatsApp Auto-Reply: $replyMessage (success=$replied)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing WhatsApp auto reply", e)
            }
        }
    }

    private fun trySendRemoteReply(context: Context, notification: Notification, replyText: String): Boolean {
        val actions = notification.actions ?: return false
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.resultKey != null) {
                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putCharSequence(remoteInput.resultKey, replyText)
                    RemoteInput.addResultsToIntent(
                        arrayOf(androidx.core.app.RemoteInput.Builder(remoteInput.resultKey).build()),
                        intent,
                        bundle
                    )
                    try {
                        action.actionIntent.send(context, 0, intent)
                        return true
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed sending RemoteInput intent", e)
                    }
                }
            }
        }
        return false
    }
}
