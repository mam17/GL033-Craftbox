package com.grl.sms_wa.sms_helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.grl.sms_wa.utils.SpManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsReader(private val context: Context) {
    private val contactInfoResolver = ContactInfoResolver(context)
    private val contactInfoCache = mutableMapOf<String, ContactInfo>()
    private val spManager by lazy { SpManager.get(context) }

    suspend fun getAllMessages(): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) return@withContext emptyList()

        queryMessages(selection = null, selectionArgs = null, sortOrder = "${Telephony.Sms.DATE} DESC")
    }

    suspend fun getLatestMessagesByThread(): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) return@withContext emptyList()

        queryLatestMessagesByThread(maxConversations = HOME_CONVERSATION_LIMIT)
    }

    suspend fun getMessagesByThread(threadId: Long): List<SmsMessageModel> = withContext(Dispatchers.IO) {
        if (!hasReadSmsPermission()) return@withContext emptyList()

        queryMessages(
            selection = "${Telephony.Sms.THREAD_ID} = ?",
            selectionArgs = arrayOf(threadId.toString()),
            sortOrder = "${Telephony.Sms.DATE} ASC"
        )
    }

    private fun queryMessages(
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String
    ): List<SmsMessageModel> {
        val messages = mutableListOf<SmsMessageModel>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        context.contentResolver.query(
            SMS_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val threadIdIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (cursor.moveToNext()) {
                messages.add(
                    cursor.toSmsMessage(
                        idIndex = idIndex,
                        threadIdIndex = threadIdIndex,
                        addressIndex = addressIndex,
                        bodyIndex = bodyIndex,
                        dateIndex = dateIndex,
                        typeIndex = typeIndex,
                        readIndex = readIndex
                    )
                )
            }
        }

        return messages
    }

    private fun queryLatestMessagesByThread(maxConversations: Int): List<SmsMessageModel> {
        val messages = mutableListOf<SmsMessageModel>()
        val seenThreadIds = mutableSetOf<Long>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        context.contentResolver.query(
            SMS_CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val threadIdIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (cursor.moveToNext() && messages.size < maxConversations) {
                val threadId = cursor.getLong(threadIdIndex)
                val address = cursor.getString(addressIndex).orEmpty()
                if (spManager.isBlocked(address) || spManager.isArchived(address)) continue
                if (!seenThreadIds.add(threadId)) continue

                messages.add(
                    cursor.toSmsMessage(
                        idIndex = idIndex,
                        threadIdIndex = threadIdIndex,
                        addressIndex = addressIndex,
                        bodyIndex = bodyIndex,
                        dateIndex = dateIndex,
                        typeIndex = typeIndex,
                        readIndex = readIndex
                    )
                )
            }
        }

        return messages
    }

    private fun android.database.Cursor.toSmsMessage(
        idIndex: Int,
        threadIdIndex: Int,
        addressIndex: Int,
        bodyIndex: Int,
        dateIndex: Int,
        typeIndex: Int,
        readIndex: Int
    ): SmsMessageModel {
        val address = getString(addressIndex).orEmpty()
        val contactInfo = contactInfoCache.getOrPut(address) {
            contactInfoResolver.resolve(address)
        }

        return SmsMessageModel(
            id = getLong(idIndex),
            threadId = getLong(threadIdIndex),
            address = address,
            body = getString(bodyIndex).orEmpty(),
            dateMillis = getLong(dateIndex),
            type = getInt(typeIndex).toSmsType(),
            isRead = getInt(readIndex) == SMS_READ,
            contactName = contactInfo.name,
            contactPhotoUri = contactInfo.photoUri
        )
    }

    private fun hasReadSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Int.toSmsType(): SmsType {
        return when (this) {
            Telephony.Sms.MESSAGE_TYPE_INBOX -> SmsType.INBOX
            Telephony.Sms.MESSAGE_TYPE_SENT -> SmsType.SENT
            Telephony.Sms.MESSAGE_TYPE_DRAFT -> SmsType.DRAFT
            Telephony.Sms.MESSAGE_TYPE_OUTBOX -> SmsType.OUTBOX
            Telephony.Sms.MESSAGE_TYPE_FAILED -> SmsType.FAILED
            Telephony.Sms.MESSAGE_TYPE_QUEUED -> SmsType.QUEUED
            else -> SmsType.UNKNOWN
        }
    }

    companion object {
        private val SMS_CONTENT_URI: Uri = Telephony.Sms.CONTENT_URI
        private const val HOME_CONVERSATION_LIMIT = 200
        private const val SMS_READ = 1
    }
}
