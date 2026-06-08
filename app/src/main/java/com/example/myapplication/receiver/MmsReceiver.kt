package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.mms.service_alt.DownloadRequest
import com.android.mms.service_alt.MmsConfig
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.notification.NotificationSMS
import com.klinker.android.send_message.MmsReceivedReceiver
import com.klinker.android.send_message.Utils
import java.io.File
import java.io.FileInputStream

class MmsReceiver : BroadcastReceiver() {
    private val smsRepository = SmsRepository()
    private lateinit var spManager: SpManager
    companion object {
        private const val TAG = "MmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        spManager = SpManager.get(context)

        val path = intent.getStringExtra(MmsReceivedReceiver.EXTRA_FILE_PATH)
        if (path == null) {
            Log.d(TAG, "No file path in intent, ignoring. Action: ${intent.action}")
            return
        }

        Log.d(TAG, "MMS finished downloading, persisting to database. Path: $path")

        val subscriptionId = intent.getIntExtra(
            MmsReceivedReceiver.SUBSCRIPTION_ID,
            Utils.getDefaultSubscriptionId()
        )
        val locationUrl = intent.getStringExtra(MmsReceivedReceiver.EXTRA_LOCATION_URL)
        val transactionId = intent.getStringExtra(MmsReceivedReceiver.EXTRA_TRANSACTION_ID)

        Thread {
            try {
                val mDownloadFile = File(path)
                if (!mDownloadFile.exists()) {
                    Log.e(TAG, "Download file does not exist: $path")
                    return@Thread
                }

                val nBytes = mDownloadFile.length().toInt()
                val response = ByteArray(nBytes)
                FileInputStream(mDownloadFile).use { it.read(response, 0, nBytes) }

                val mmsConfig = MmsConfig.Overridden(MmsConfig(context), null)

                // Persist the message to the database
                val messageUri = DownloadRequest.persist(
                    context, response, mmsConfig,
                    locationUrl, transactionId, subscriptionId, null
                )

                Log.d(TAG, "MMS saved successfully: $messageUri")
                mDownloadFile.delete()

                // Show notification
                if (messageUri != null) {
                    val mmsId = messageUri.lastPathSegment ?: ""
                    val sender = smsRepository.getMmsDisplayAddress(context, mmsId, 1) // 1 = Inbox
                    if (sender.isNotEmpty()) {
                        var body = smsRepository.getMmsText(context, mmsId)
                        if (body.isEmpty()) {
                            body = "Không có tiêu đề"
                        }
                        if (!spManager.isBlocked(sender)) {
                            NotificationSMS.showNotification(context, sender, body)
                        } else {
                            Log.d(TAG, "MMS from blocked sender: $sender. Skipping notification.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting MMS", e)
            }
        }.start()
    }
}
