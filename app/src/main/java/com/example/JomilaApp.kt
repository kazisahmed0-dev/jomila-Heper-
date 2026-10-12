package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.example.data.db.AppDatabase

class JomilaApp : Application() {

    lateinit var database: AppDatabase
        private set

    companion object {
        const val CHANNEL_REMINDERS = "channel_jomila_reminders"
        const val CHANNEL_CALLS = "channel_jomila_calls"
        const val CHANNEL_WHATSAPP = "channel_jomila_whatsapp"
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Reminder channel with alarm ringtone
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "জমিলা রিমাইন্ডার ও অ্যালার্ম",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "সময়মতো স্কুলে যাওয়া বা প্রয়োজনীয় কাজের অ্যালার্ম"
                enableVibration(true)
                setSound(alarmSound, audioAttributes)
            }

            // Calls channel
            val callChannel = NotificationChannel(
                CHANNEL_CALLS,
                "জমিলা কল রিসেপশনিস্ট",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ইনকামিং বা মিসড কলের নোটিফিকেশন ও অটো রেসপন্স"
                enableVibration(true)
            }

            // WhatsApp responder channel
            val whatsappChannel = NotificationChannel(
                CHANNEL_WHATSAPP,
                "জমিলা হোয়াটসঅ্যাপ অ্যাক্টিভিটি",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "হোয়াটসঅ্যাপ মেসেজে স্বয়ংক্রিয় উত্তরের লগ"
            }

            notificationManager.createNotificationChannels(
                listOf(reminderChannel, callChannel, whatsappChannel)
            )
        }
    }
}
