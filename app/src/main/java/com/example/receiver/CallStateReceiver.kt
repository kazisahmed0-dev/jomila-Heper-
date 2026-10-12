package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.example.JomilaApp
import com.example.MainActivity
import com.example.data.db.AppDatabase
import com.example.data.model.CallActivityLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "অপরিচিত নম্বর"

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            logAndNotifyCall(context, incomingNumber, "INCOMING")
        } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            // Call ended or missed
        }
    }

    private fun logAndNotifyCall(context: Context, number: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.appDao().insertCallLog(
                    CallActivityLog(
                        phoneNumber = number,
                        callerName = "ইনকামিং কলার",
                        type = type,
                        responseNote = "জমিলা স্মার্ট রিসেপশনিস্ট স্ট্যান্ডবাই"
                    )
                )

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "receptionist")
                    putExtra("incoming_number", number)
                }

                val pi = PendingIntent.getActivity(
                    context,
                    number.hashCode(),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, JomilaApp.CHANNEL_CALLS)
                    .setSmallIcon(android.R.drawable.stat_notify_missed_call)
                    .setContentTitle("📞 জমিলা কল রিসেপশনিস্ট")
                    .setContentText("$number থেকে কল এসেছে। জমিলা দিয়ে কি স্মার্ট উত্তর পাঠাবেন?")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build()

                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify((System.currentTimeMillis() % 100000).toInt(), notification)
            } catch (_: Exception) {}
        }
    }
}
