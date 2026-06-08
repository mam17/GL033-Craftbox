package com.example.myapplication.receiver

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.widget.Toast
import androidx.core.net.toUri
import com.example.myapplication.R
import com.example.myapplication.data.repository.SmsRepository
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MmsSentReceiver : SendStatusReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    override fun updateAndroidDatabase(context: Context, intent: Intent, receiverResultCode: Int) {
        val uriString = intent.getStringExtra(EXTRA_CONTENT_URI) ?: return
        val uri = uriString.toUri()
        val originalResentMessageId = intent.getLongExtra(EXTRA_ORIGINAL_RESENT_MESSAGE_ID, -1L)

        val messageBox = if (receiverResultCode == Activity.RESULT_OK) {
            Telephony.Mms.MESSAGE_BOX_SENT
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    context.getString(R.string.txt_failed_to_send),
                    Toast.LENGTH_SHORT
                ).show()
            }
            Telephony.Mms.MESSAGE_BOX_FAILED
        }

        val values = ContentValues(1).apply {
            put(Telephony.Mms.MESSAGE_BOX, messageBox)
        }

        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        // Handle cleanup of temporary files if the library created any
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    override fun updateAppDatabase(context: Context, intent: Intent, receiverResultCode: Int) {
        smsRepository.notifyDataChanged()
    }

    companion object {
        private const val EXTRA_CONTENT_URI = "content_uri"
        private const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_ORIGINAL_RESENT_MESSAGE_ID = "original_message_id"
    }
}
