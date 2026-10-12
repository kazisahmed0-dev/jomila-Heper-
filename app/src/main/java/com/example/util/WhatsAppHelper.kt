package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.service.WhatsAppNotificationService

object WhatsAppHelper {

    fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String) {
        try {
            val cleanNumber = phoneNumber.replace("+", "").replace("-", "").replace(" ", "").trim()
            val uri = if (cleanNumber.isNotBlank()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}")
            } else {
                Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }

            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                // If package com.whatsapp fails, open standard intent
                val genericIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(genericIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "হোয়াটসঅ্যাপ খুলতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == packageName) {
                return true
            }
        }
        return false
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "সেটিংস খুলতে পারা যায়নি", Toast.LENGTH_SHORT).show()
        }
    }
}
