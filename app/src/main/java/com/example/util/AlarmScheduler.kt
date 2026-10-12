package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleReminder(context: Context, reminderId: Long, title: String, timeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TITLE, title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled reminder: $title at $timeMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun formatBanglaTime(timeMillis: Long): String {
        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val rawTime = sdfTime.format(Date(timeMillis))
        val sdfDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val rawDate = sdfDate.format(Date(timeMillis))

        val period = if (rawTime.contains("AM", ignoreCase = true)) "সকাল" else "রাত/বিকাল"
        val cleanTime = rawTime.replace("AM", "", true).replace("PM", "", true).trim()

        return "$period $cleanTime ($rawDate)"
    }
}
