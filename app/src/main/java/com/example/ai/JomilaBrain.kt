package com.example.ai

import android.content.Context
import com.example.data.model.ChatMessage
import com.example.data.model.ReminderItem
import com.example.data.model.TaskItem
import com.example.data.repository.JomilaRepository
import com.example.util.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

data class BrainResult(
    val replyText: String,
    val actionType: String = ChatMessage.ACTION_NONE,
    val actionPayload: String = "",
    val spokenText: String = replyText
)

class JomilaBrain(private val repository: JomilaRepository) {

    private val systemPrompt = """
        You are 'Jomila' (জমিলা), an exceptionally helpful, polite, intelligent Bengali virtual AI assistant and organizer.
        The user speaks in Bengali, Banglish (Bengali written in English letters), or English.
        
        Your responsibilities:
        1. Schedule Reminders: E.g., '৭ টায় স্কুলে যাবো রিমাইন্ডার দাও' (Remind me at 7 to go to school).
        2. Manage Task Lists: E.g., 'আমার কাজের লিস্ট মনে রাখো এবং শেষ করতে সাহায্য করো' (Keep track of my tasks and help me finish them).
        3. Draft WhatsApp Messages: E.g., 'অমুককে হোয়াটসঅ্যাপে বলো যে কালকে আসবো না' (Message someone on WhatsApp that I won't come tomorrow).
        4. Phone Calls: E.g., 'রহিমকে ফোন করো' (Call Rahim).
        5. General Assistance & Auto-Reply advice: Be warm, respectful, friendly ('ভাইয়া/আপু', 'অবশ্যই').

        Always respond with a JSON object format:
        {
          "reply": "Your friendly Bengali spoken response to the user",
          "action": "REMINDER" or "TASK" or "WHATSAPP" or "CALL" or "NONE",
          "payload": {
             "title": "...",
             "timeHour": 7,
             "timeMinute": 0,
             "phoneNumber": "",
             "recipientName": "",
             "message": "",
             "priority": "উচ্চ" or "মাঝারি"
          }
        }
        Respond strictly with the JSON object.
    """.trimIndent()

    suspend fun processQuery(userInput: String, context: Context): BrainResult = withContext(Dispatchers.IO) {
        val trimmed = userInput.trim()
        if (trimmed.isBlank()) {
            return@withContext BrainResult(replyText = "জ্বী, কিছু বলুন! আমি শুনছি।")
        }

        // Try AI response via Gemini first if configured
        if (GeminiClient.isApiKeyConfigured()) {
            val aiResult = GeminiClient.generateResponse(
                prompt = "User Command: $trimmed",
                systemInstruction = systemPrompt
            )
            if (aiResult.isSuccess) {
                val rawText = aiResult.getOrNull() ?: ""
                val parsed = tryParseJsonResponse(rawText, trimmed, context)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
        }

        // Reliable offline heuristic & rule-based processor
        processOfflineCommand(trimmed, context)
    }

    private suspend fun tryParseJsonResponse(rawText: String, originalInput: String, context: Context): BrainResult? {
        return try {
            val jsonStart = rawText.indexOf('{')
            val jsonEnd = rawText.lastIndexOf('}')
            if (jsonStart == -1 || jsonEnd == -1 || jsonEnd <= jsonStart) return null

            val jsonStr = rawText.substring(jsonStart, jsonEnd + 1)
            val json = JSONObject(jsonStr)

            val reply = json.optString("reply", "আমি আপনার কাজ সম্পন্ন করেছি!")
            val action = json.optString("action", "NONE")
            val payload = json.optJSONObject("payload")

            when (action) {
                "REMINDER" -> {
                    val title = payload?.optString("title")?.ifBlank { "রিমাইন্ডার" } ?: "রিমাইন্ডার: $originalInput"
                    val hour = payload?.optInt("timeHour", 7) ?: 7
                    val minute = payload?.optInt("timeMinute", 0) ?: 0
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    val reminderId = repository.addReminder(
                        ReminderItem(
                            title = title,
                            timeMillis = cal.timeInMillis,
                            timeDisplay = String.format("%02d:%02d", hour, minute),
                            category = if (title.contains("স্কুল", true) || title.contains("school", true)) "স্কুল" else "দৈনন্দিন"
                        )
                    )
                    AlarmScheduler.scheduleReminder(context, reminderId, title, cal.timeInMillis)
                    BrainResult(
                        replyText = "$reply\n\n⏰ রিমাইন্ডার সেট করা হয়েছে: $title (${AlarmScheduler.formatBanglaTime(cal.timeInMillis)})",
                        actionType = ChatMessage.ACTION_REMINDER,
                        actionPayload = "$reminderId||$title||${cal.timeInMillis}"
                    )
                }

                "TASK" -> {
                    val title = payload?.optString("title")?.ifBlank { originalInput } ?: originalInput
                    val priority = payload?.optString("priority", "মাঝারি") ?: "মাঝারি"
                    val subtasks = "১. শুরু করুন\n২. কাজের ধাপগুলো সম্পন্ন করুন\n৩. জমিলাকে আপডেট দিন"
                    val taskId = repository.addTask(
                        TaskItem(
                            title = title,
                            priority = priority,
                            category = "কাজের তালিকা",
                            aiSubtasks = subtasks
                        )
                    )
                    BrainResult(
                        replyText = "$reply\n\n📋 কাজের তালিকায় যুক্ত করা হয়েছে: \"$title\"",
                        actionType = ChatMessage.ACTION_TASK,
                        actionPayload = "$taskId||$title"
                    )
                }

                "WHATSAPP" -> {
                    val recipient = payload?.optString("recipientName", "") ?: ""
                    val msg = payload?.optString("message")?.ifBlank { "আমি কালকে আসতে পারব না।" } ?: "আমি কালকে আসতে পারব না।"
                    val phone = payload?.optString("phoneNumber", "") ?: ""
                    BrainResult(
                        replyText = "$reply\n\n💬 হোয়াটসঅ্যাপ মেসেজ তৈরি করা হয়েছে:\n\"$msg\"",
                        actionType = ChatMessage.ACTION_WHATSAPP,
                        actionPayload = "$recipient||$phone||$msg"
                    )
                }

                "CALL" -> {
                    val phone = payload?.optString("phoneNumber", "") ?: ""
                    val recipient = payload?.optString("recipientName", "") ?: ""
                    BrainResult(
                        replyText = "$reply\n\n📞 কল করার জন্য প্রস্তুত: ${if (recipient.isNotBlank()) recipient else phone}",
                        actionType = ChatMessage.ACTION_CALL,
                        actionPayload = "$recipient||$phone"
                    )
                }

                else -> BrainResult(replyText = reply)
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun processOfflineCommand(input: String, context: Context): BrainResult {
        val lower = input.lowercase()

        // 1. Reminder command: e.g. "৭ টায় স্কুলে যাবো", "7 tar somoy school a jabo remaind koe dibe", "remind me at 7"
        val reminderMatch = Regex("(\\d{1,2}|[০-৯]{1,2})\\s*(?:টা|টার|tar|somoy|সময়|am|pm)?").find(input)
        val isReminderIntent = lower.contains("remaind") || lower.contains("remind") || lower.contains("মনে") ||
                lower.contains("স্কুল") || lower.contains("school") || lower.contains("alarm") || lower.contains("অ্যালার্ম")

        if (reminderMatch != null && (isReminderIntent || lower.contains("যাবো") || lower.contains("jabo"))) {
            var hour = convertBanglaToEnglishDigits(reminderMatch.groupValues[1]).toIntOrNull() ?: 7
            if (hour in 1..6 && (lower.contains("রাত") || lower.contains("সন্ধ্যা") || lower.contains("pm"))) {
                hour += 12
            }
            if (hour > 23) hour = 7

            val title = if (lower.contains("school") || lower.contains("স্কুল")) {
                "স্কুলে যাওয়ার প্রস্তুতি ও রওনা দেওয়া"
            } else {
                "রিমাইন্ডার: $input"
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val reminderId = repository.addReminder(
                ReminderItem(
                    title = title,
                    timeMillis = cal.timeInMillis,
                    timeDisplay = String.format("%02d:00", hour),
                    category = if (lower.contains("school") || lower.contains("স্কুল")) "স্কুল" else "দৈনন্দিন"
                )
            )

            AlarmScheduler.scheduleReminder(context, reminderId, title, cal.timeInMillis)

            return BrainResult(
                replyText = "ঠিক আছে ভাইয়া! আমি সময়মতো আপনাকে মনে করিয়ে দেব।\n" +
                        "⏰ রিমাইন্ডার সেট করা হলো: $title\n" +
                        "সময়: ${AlarmScheduler.formatBanglaTime(cal.timeInMillis)}",
                actionType = ChatMessage.ACTION_REMINDER,
                actionPayload = "$reminderId||$title||${cal.timeInMillis}",
                spokenText = "ঠিক আছে! আমি সময়মতো আপনাকে মনে করিয়ে দেব।"
            )
        }

        // 2. WhatsApp message drafting: e.g. "omuk ke massage koro je ami kalke asbo na", "হোয়াটসঅ্যাপে মেসেজ করো"
        if (lower.contains("whatsapp") || lower.contains("হোয়াটসঅ্যাপ") || lower.contains("massage") || lower.contains("মেসেজ") || lower.contains("asbo na") || lower.contains("আসবো না")) {
            val messageBody = if (lower.contains("asbo na") || lower.contains("আসবো না") || lower.contains("kalke") || lower.contains("কালকে")) {
                "আসসালামু আলাইকুম, আমি জরুরি কারণে আগামীকাল আসতে পারব না।"
            } else {
                "আসসালামু আলাইকুম, আমি এখন ব্যস্ত আছি, পরে কথা বলছি।"
            }

            val target = extractRecipientName(input)

            return BrainResult(
                replyText = "অবশ্যই! আমি হোয়াটসঅ্যাপের জন্য মেসেজটি ড্রাফট করে রেখেছি:\n" +
                        "💬 \"$messageBody\"\n\n" +
                        "নিচের বাটনে ট্যাপ করে সরাসরি হোয়াটসঅ্যাপে পাঠাতে পারেন।",
                actionType = ChatMessage.ACTION_WHATSAPP,
                actionPayload = "$target||||$messageBody",
                spokenText = "অবশ্যই! হোয়াটসঅ্যাপ মেসেজ রেডি করে দিয়েছি।"
            )
        }

        // 3. Call action: e.g. "omuk ke phone koro", "কাউকে কল করো", "phone koro"
        if (lower.contains("phone") || lower.contains("ফোন") || lower.contains("call") || lower.contains("কল")) {
            val target = extractRecipientName(input)
            val cleanPhone = target.filter { it.isDigit() }
            val displayName = if (target.isNotBlank()) target else "নির্দিষ্ট নম্বর"

            return BrainResult(
                replyText = "জ্বী, $displayName কে কল করার জন্য ডায়ালার ওপেন করতে নিচের বাটনে চাপুন।",
                actionType = ChatMessage.ACTION_CALL,
                actionPayload = "$displayName||$cleanPhone",
                spokenText = "কল করার জন্য ডায়ালার প্রস্তুত।"
            )
        }

        // 4. Task / Todo list management: e.g. "amar kajer list ta mone rekhe amake shesh korte shahajyo korbe"
        if (lower.contains("kaj") || lower.contains("কাজ") || lower.contains("list") || lower.contains("লিস্ট") || lower.contains("task") || lower.contains("help") || lower.contains("সাহায্য")) {
            val taskTitle = if (input.length > 20 && !lower.startsWith("amar kajer list")) input else "আজকের কাজের তালিকা ও প্রস্তুতি"
            val aiSteps = "১. প্রয়োজনীয় জিনিস ও বইপত্র গুছিয়ে রাখা\n২. অগ্রাধিকার অনুযায়ী প্রথম কাজটি শেষ করা\n৩. সম্পন্ন হলে জমিলা অ্যাপে টিক দিয়ে দেওয়া"
            
            val taskId = repository.addTask(
                TaskItem(
                    title = taskTitle,
                    notes = "জমিলা স্বয়ংক্রিয়ভাবে আপনাকে এই কাজ শেষ করতে সহায়তা করবে।",
                    priority = "উচ্চ",
                    category = "দৈনন্দিন",
                    aiSubtasks = aiSteps
                )
            )

            return BrainResult(
                replyText = "নিশ্চয়ই! আপনার কাজের তালিকা আমি যত্ন করে মনে রাখব এবং একে একে শেষ করতে সাহায্য করব।\n" +
                        "📋 নতুন কাজ যুক্ত হয়েছে: \"$taskTitle\"\n\n" +
                        "সহায়ক ধাপসমূহ:\n$aiSteps",
                actionType = ChatMessage.ACTION_TASK,
                actionPayload = "$taskId||$taskTitle",
                spokenText = "আপনার কাজের তালিকা আমি মনে রাখব এবং শেষ করতে সাহায্য করব।"
            )
        }

        // 5. Default friendly Bengali assistant chat
        val greetings = listOf(
            "হ্যালো ভাইয়া! জমিলা সব সময় আপনার পাশে আছে। আপনি যে কোনো রিমাইন্ডার, কাজের লিস্ট বা মেসেজ ড্রাফট করতে বলতে পারেন।",
            "জ্বী! আমি শুনছি। আমাকে বলুন: '৭ টায় স্কুলে যাওয়ার রিমাইন্ডার দাও' বা 'কালকে আসবো না হোয়াটসঅ্যাপে মেসেজ পাঠাও'!",
            "আসসালামু আলাইকুম! জমিলা হেল্পার দিয়ে আপনার দৈনন্দিন কাজগুলো আরও সহজ ও সময়নিষ্ঠ করে তুলুন।"
        )
        return BrainResult(replyText = greetings.random())
    }

    private fun extractRecipientName(input: String): String {
        val parts = input.split(" ")
        for (i in 0 until parts.size - 1) {
            val p = parts[i].lowercase()
            if (p == "ke" || p == "কে" || p == "to") {
                return parts.getOrNull(i - 1) ?: ""
            }
        }
        return ""
    }

    private fun convertBanglaToEnglishDigits(banglaDigits: String): String {
        val banglaNumbers = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in banglaDigits) {
            val idx = banglaNumbers.indexOf(ch)
            if (idx != -1) {
                sb.append(idx)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
