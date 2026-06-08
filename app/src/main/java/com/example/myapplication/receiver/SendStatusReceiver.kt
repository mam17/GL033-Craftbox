package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class SendStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receiverResultCode = resultCode
        Thread {
            updateAndroidDatabase(context, intent, receiverResultCode)
            updateAppDatabase(context, intent, receiverResultCode)
        }.start()
    }

    abstract fun updateAndroidDatabase(context: Context, intent: Intent, receiverResultCode: Int)

    abstract fun updateAppDatabase(context: Context, intent: Intent, receiverResultCode: Int)

    companion object {
        const val SMS_SENT_ACTION = "com.simplemobiletools.smsmessenger.SMS_SENT"
        const val SMS_DELIVERED_ACTION = "com.simplemobiletools.smsmessenger.SMS_DELIVERED"
        const val EXTRA_SUB_ID = "sub_id"
        const val NO_ERROR_CODE = -1
    }
}
