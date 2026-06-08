package com.example.myapplication.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.utils.ScheduledMessageScheduler
import com.example.myapplication.utils.SpManager

class ScheduledMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(ScheduledMessageScheduler.EXTRA_ID).orEmpty()
        val address = intent.getStringExtra(ScheduledMessageScheduler.EXTRA_ADDRESS).orEmpty()
        val body = intent.getStringExtra(ScheduledMessageScheduler.EXTRA_BODY).orEmpty()
        val subscriptionId = intent.getIntExtra(
            ScheduledMessageScheduler.EXTRA_SUBSCRIPTION_ID,
            -1
        )

        if (id.isBlank() || address.isBlank() || body.isBlank()) return
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            getSmsManager(context, subscriptionId).sendTextMessage(address, null, body, null, null)
            SpManager.get(context).markScheduledMessageSent(id)
            SmsRepository().insertSentSms(context, address, body, System.currentTimeMillis())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSmsManager(context: Context, subscriptionId: Int): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val smsManager = context.getSystemService(SmsManager::class.java)
            if (subscriptionId != -1) {
                smsManager.createForSubscriptionId(subscriptionId)
            } else {
                smsManager
            }
        } else {
            @Suppress("DEPRECATION")
            if (subscriptionId != -1) {
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getDefault()
            }
        }
    }
}
