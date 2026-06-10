package com.grl.sms_wa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.grl.sms_wa.data.repository.SmsRepository
import com.grl.sms_wa.utils.PermissionUtils
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.notification.NotificationSMS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var spManager: SpManager

    override fun onReceive(context: Context, intent: Intent) {
        val isDeliverAction = intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION
        val isReceivedAction = intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        if (isDeliverAction || isReceivedAction
        ) {
            if (isReceivedAction && PermissionUtils.isDefaultSmsApp(context)) return

            Log.d("SmsReceiver", "SMS received")
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages != null && messages.isNotEmpty()) {
                val address = messages[0].originatingAddress ?: ""

                if (spManager.isBlocked(address)) {
                    Log.d("SmsReceiver", "SMS from blocked address: $address. Skipping notification.")
                    return
                }
                val body = StringBuilder()
                for (message in messages) {
                    body.append(message.messageBody)
                }
                
                if (isDeliverAction) {
                    smsRepository.insertSms(
                        context = context,
                        address = address,
                        body = body.toString(),
                        date = System.currentTimeMillis(),
                        read = 0
                    )
                }

                // Show Notification
                NotificationSMS.showNotification(context, address, body.toString())
            }
        }
    }
}
