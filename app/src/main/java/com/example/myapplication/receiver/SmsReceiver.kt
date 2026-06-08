package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.notification.NotificationSMS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var spManager: SpManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            Log.d("SmsReceiver", "SMS received")
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages != null && messages.isNotEmpty()) {
                val address = messages[0].originatingAddress ?: ""

                if (spManager.isBlocked(address)) {
                    Log.d("SmsReceiver", "SMS from blocked address: $address. Skipping notification.")
                }
                val body = StringBuilder()
                for (message in messages) {
                    body.append(message.messageBody)
                }
                
                smsRepository.insertSms(
                    context = context,
                    address = address,
                    body = body.toString(),
                    date = System.currentTimeMillis(),
                    read = 0
                )

                // Show Notification
                if (!spManager.isBlocked(address)) {
                    NotificationSMS.showNotification(context, address, body.toString())
                }
            }
        }
    }
}
