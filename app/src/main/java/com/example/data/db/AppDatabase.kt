package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AutoReplyRule
import com.example.data.model.CallActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.ReminderItem
import com.example.data.model.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        ChatMessage::class,
        ReminderItem::class,
        TaskItem::class,
        AutoReplyRule::class,
        CallActivityLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jomila_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default starter data
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).appDao()

                                // Welcome message from Jomila
                                dao.insertMessage(
                                    ChatMessage(
                                        text = "আসসালামু আলাইকুম! আমি জমিলা, আপনার ভার্চুয়াল অ্যাসিস্ট্যান্ট।\n" +
                                                "আমি আপনাকে রিমাইন্ডার দেওয়া, কাজের তালিকা মনে রাখা, হোয়াটসঅ্যাপ মেসেজের উত্তর দেওয়া এবং কলের যত্ন নিতে সাহায্য করব।\n" +
                                                "নিচের বাটনে চাপুন বা আমাকে বাংলায় লিখে বলুন!",
                                        isUser = false
                                    )
                                )

                                // Initial default WhatsApp auto-reply rules
                                dao.insertRule(
                                    AutoReplyRule(
                                        triggerPattern = "any",
                                        replyText = "ধন্যবাদ মেসেজের জন্য! আমি এখন একটু ব্যস্ত আছি, কিছুক্ষণ পর জমিলা আমাকে মনে করিয়ে দিলে উত্তর দেব।",
                                        isEnabled = true,
                                        isAiGenerated = false
                                    )
                                )
                                dao.insertRule(
                                    AutoReplyRule(
                                        triggerPattern = "kal|কাল|কালকে|asbo|আসবো",
                                        replyText = "প্রিয়, আমি আগামীকাল আসতে পারব না। জমিলা হেল্পার মারফত জানিয়ে দেওয়া হলো।",
                                        isEnabled = true,
                                        isAiGenerated = false
                                    )
                                )
                                dao.insertRule(
                                    AutoReplyRule(
                                        triggerPattern = "urgent|জরুরি",
                                        replyText = "জরুরি মেসেজ পেয়েছি! জমিলা দ্রুত আমাকে অ্যালার্ট করেছে, আমি লাইনে আসছি।",
                                        isEnabled = true,
                                        isAiGenerated = false
                                    )
                                )

                                // Sample starter task
                                dao.insertTask(
                                    TaskItem(
                                        title = "স্কুলের প্রজেক্ট জমা দেওয়া ও পড়া রিভিশন",
                                        notes = "বই-খাতা গুছিয়ে রাখা এবং অ্যাসাইনমেন্ট শেষ করা",
                                        priority = "উচ্চ",
                                        category = "পড়াশোনা",
                                        aiSubtasks = "১. রেফারেন্স বই পড়া\n২. নোট তৈরি করা\n৩. ব্যাগ গুছিয়ে রাখা"
                                    )
                                )

                                // Sample starter reminder at 7:00 AM (tomorrow or today)
                                val calendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 7)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    if (timeInMillis <= System.currentTimeMillis()) {
                                        add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                }
                                dao.insertReminder(
                                    ReminderItem(
                                        title = "সকাল ৭:০০ - স্কুলে যাওয়ার সময়",
                                        timeMillis = calendar.timeInMillis,
                                        timeDisplay = "সকাল ০৭:০০",
                                        category = "স্কুল",
                                        isActive = true
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
