package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PhoneCallHelper {

    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.trim()
            val uri = Uri.parse("tel:$cleanNumber")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ডায়াল করা সম্ভব হয়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val uri = Uri.parse("smsto:${phoneNumber.trim()}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "মেসেজ পাঠানো সম্ভব হয়নি: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
