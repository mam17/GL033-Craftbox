package com.example.myapplication.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmsSentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(Constant.EXTRA_MESSAGE_ID).orEmpty()
        if (messageId.isBlank()) return

        val type = if (resultCode == Activity.RESULT_OK) {
            Telephony.Sms.MESSAGE_TYPE_SENT
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.txt_failed_to_send),
                Toast.LENGTH_SHORT
            ).show()
            Telephony.Sms.MESSAGE_TYPE_FAILED
        }

        smsRepository.updateSmsType(context, messageId, type)
        smsRepository.notifyDataChanged()
    }
}
