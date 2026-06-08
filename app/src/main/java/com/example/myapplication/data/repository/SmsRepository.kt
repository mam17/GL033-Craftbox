package com.example.myapplication.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Telephony
import androidx.core.net.toUri
import com.example.myapplication.data.model.SmsMessage
import com.example.myapplication.utils.PermissionUtils
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.example.myapplication.receiver.MmsSentReceiver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepository @Inject constructor() {

    private val _refreshFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshFlow = _refreshFlow.asSharedFlow()

    fun notifyDataChanged() {
        _refreshFlow.tryEmit(Unit)
    }

    fun getConversationList(context: Context): List<SmsMessage> {
        val smsList = mutableListOf<SmsMessage>()
        if (!PermissionUtils.hasSmsPermission(context)) {
            return smsList
        }
        val uri: Uri = "content://mms-sms/conversations?simple=true".toUri()

        val cursor = context.contentResolver.query(
            uri,
            null,
            null,
            null,
            "date DESC"
        )

        cursor?.use {
            val snippetIndex = it.getColumnIndex("snippet")
            val dateIndex = it.getColumnIndex("date")
            val readIndex = it.getColumnIndex("read")
            val typeIndex = it.getColumnIndex("type")
            val threadIdIndex = it.getColumnIndex("_id")
            val recipientIdIndex = it.getColumnIndex("recipient_ids")

            while (it.moveToNext()) {
                val threadId = if (threadIdIndex != -1) it.getString(threadIdIndex) else ""
                val recipientIds =
                    if (recipientIdIndex != -1) it.getString(recipientIdIndex) ?: "" else ""
                var body = if (snippetIndex != -1) it.getString(snippetIndex) ?: "" else ""
                if (body.startsWith("[[media_uri]]")) {
                    val split = body.split("[[body]]", limit = 2)
                    body = if (split.size == 2) split[1] else body.replace("[[media_uri]]", "")
                }

                if (body.isEmpty()) {
                    body = "No title"
                }
                val date = if (dateIndex != -1) it.getLong(dateIndex) else 0L
                val read = if (readIndex != -1) it.getInt(readIndex) else 1

                val address = getAddressByRecipientIds(context, recipientIds)

                smsList.add(
                    SmsMessage(
                        id = threadId,
                        address = address,
                        body = body,
                        date = date,
                        read = read,
                        type = 1,
                        threadId = threadId
                    )
                )
            }
        }
        return smsList
    }

    private fun getAddressByRecipientIds(context: Context, recipientIds: String): String {
        if (recipientIds.isEmpty()) return ""
        val ids = recipientIds.split(" ")
        val addresses = mutableListOf<String>()
        for (id in ids) {
            if (id.isEmpty()) continue
            try {
                val uri = Uri.parse("content://mms-sms/canonical-addresses/$id")
                context.contentResolver.query(uri, arrayOf("address"), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            addresses.add(cursor.getString(0) ?: "")
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return addresses.joinToString(", ")
    }

    fun getAllSms(context: Context): List<SmsMessage> {
        val allMessages = mutableListOf<SmsMessage>()
        if (!PermissionUtils.hasSmsPermission(context)) {
            return allMessages
        }

        // Fetch SMS
        val smsUri: Uri = Telephony.Sms.CONTENT_URI
        val smsProjection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID
        )

        context.contentResolver.query(smsUri, smsProjection, null, null, "date DESC")
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    allMessages.add(
                        SmsMessage(
                            id = cursor.getString(0),
                            address = cursor.getString(1) ?: "",
                            body = cursor.getString(2) ?: "",
                            date = cursor.getLong(3),
                            read = cursor.getInt(4),
                            type = cursor.getInt(5),
                            threadId = cursor.getString(6)
                        )
                    )
                }
            }

        // Fetch MMS
        val mmsUri: Uri = Telephony.Mms.CONTENT_URI
        val mmsProjection = arrayOf(
            Telephony.Mms._ID,
            Telephony.Mms.DATE,
            Telephony.Mms.READ,
            Telephony.Mms.MESSAGE_BOX,
            Telephony.Mms.THREAD_ID
        )

        context.contentResolver.query(mmsUri, mmsProjection, null, null, "date DESC")
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    val mmsId = cursor.getString(0)
                    val date = cursor.getLong(1) * 1000 // MMS date is in seconds
                    val read = cursor.getInt(2)
                    val msgBox = cursor.getInt(3)
                    val threadId = cursor.getString(4)

                    val displayAddress = getMmsDisplayAddress(context, mmsId, msgBox)
                    if (displayAddress.isNotEmpty()) {
                        val address = displayAddress
                        var body = getMmsText(context, mmsId)
                        val mediaUri = getMmsImage(context, mmsId)

                        if (body.isEmpty()) {
                            body = "No title"
                        }

                        allMessages.add(
                            SmsMessage(
                                id = mmsId,
                                address = address,
                                body = body,
                                date = date,
                                read = read,
                                type = when (msgBox) {
                                    1 -> Telephony.Sms.MESSAGE_TYPE_INBOX
                                    2 -> Telephony.Sms.MESSAGE_TYPE_SENT
                                    4 -> Telephony.Sms.MESSAGE_TYPE_OUTBOX
                                    5 -> Telephony.Sms.MESSAGE_TYPE_FAILED
                                    else -> Telephony.Sms.MESSAGE_TYPE_SENT
                                },
                                mediaUri = mediaUri?.toString(),
                                threadId = threadId
                            )
                        )
                    }
                }
            }

        return allMessages.sortedByDescending { it.date }
    }

    fun getMessagesByAddress(context: Context, address: String): List<SmsMessage> {
        return getMessagesByAddresses(context, listOf(address))
    }

    fun getMessagesByAddresses(context: Context, addresses: List<String>): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        if (addresses.isEmpty() || !PermissionUtils.hasSmsPermission(context)) return messages

        // 1. Fetch SMS
        val smsUri: Uri = Telephony.Sms.CONTENT_URI
        val smsProjection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ,
            Telephony.Sms.TYPE
        )
        val placeholders = addresses.joinToString(",") { "?" }
        val smsSelection = "${Telephony.Sms.ADDRESS} IN ($placeholders)"
        val selectionArgs = addresses.toTypedArray()

        context.contentResolver.query(smsUri, smsProjection, smsSelection, selectionArgs, null)
            ?.use { cursor ->
                val idIndex = cursor.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
                val readIndex = cursor.getColumnIndex(Telephony.Sms.READ)
                val typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex)
                    val mAddress = cursor.getString(addressIndex)
                    var body = cursor.getString(bodyIndex) ?: ""
                    val date = cursor.getLong(dateIndex)
                    val read = cursor.getInt(readIndex)
                    val type = cursor.getInt(typeIndex)

                    var mediaUri: String? = null
                    if (body.startsWith("[[media_uri]]")) {
                        val content = body.substring("[[media_uri]]".length)
                        val split = content.split("[[body]]", limit = 2)
                        if (split.size >= 1) {
                            mediaUri = split[0]
                            body = if (split.size == 2) split[1] else ""
                        }
                    }
                    messages.add(
                        SmsMessage(
                            id,
                            mAddress,
                            body,
                            date,
                            read,
                            type,
                            mediaUri = mediaUri
                        )
                    )
                }
            }

        // 2. Fetch MMS
        val mmsUri = Uri.parse("content://mms")
        // Note: MMS filtering by address is complex because addresses are in 'addr' table
        // For simplicity, we fetch all and filter by threadId if possible, or just fetch all MMS for these addresses
        val mmsProjection = arrayOf("_id", "date", "read", "msg_box", "thread_id")

        context.contentResolver.query(mmsUri, mmsProjection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndex("_id")
            val dateIndex = cursor.getColumnIndex("date")
            val readIndex = cursor.getColumnIndex("read")
            val msgBoxIndex = cursor.getColumnIndex("msg_box")
            val threadIdIndex = cursor.getColumnIndex("thread_id")

            while (cursor.moveToNext()) {
                val mmsId = cursor.getString(idIndex)
                val date = cursor.getLong(dateIndex) * 1000 // MMS date is in seconds
                val read = cursor.getInt(readIndex)
                val msgBox = cursor.getInt(msgBoxIndex)
                val threadId = cursor.getString(threadIdIndex)

                // Check if this MMS belongs to our addresses
                val addrMap = getMmsAddresses(context, mmsId)
                val allMmsAddresses = addrMap.values.flatten()
                val isRelevant = allMmsAddresses.any { mAddr ->
                    addresses.any { addr ->
                        val cleanMAddr = mAddr.replace(" ", "").replace("-", "").replace("+", "")
                        val cleanAddr = addr.replace(" ", "").replace("-", "").replace("+", "")
                        cleanMAddr == cleanAddr || (cleanMAddr.length >= 9 && cleanAddr.endsWith(
                            cleanMAddr.takeLast(9)
                        ))
                    }
                }

                if (isRelevant) {
                    val body = getMmsText(context, mmsId)
                    val mediaUri = getMmsImage(context, mmsId)

                    // Determine the display address (sender for Inbox, recipient for Sent)
                    val displayAddress = getMmsDisplayAddress(context, mmsId, msgBox)

                    val type = when (msgBox) {
                        1 -> Telephony.Sms.MESSAGE_TYPE_INBOX
                        2 -> Telephony.Sms.MESSAGE_TYPE_SENT
                        4 -> Telephony.Sms.MESSAGE_TYPE_OUTBOX
                        5 -> Telephony.Sms.MESSAGE_TYPE_FAILED
                        else -> Telephony.Sms.MESSAGE_TYPE_SENT
                    }

                    messages.add(
                        SmsMessage(
                            mmsId,
                            displayAddress,
                            body,
                            date,
                            read,
                            type,
                            mediaUri = mediaUri?.toString(),
                            threadId = threadId
                        )
                    )
                }
            }
        }

        return messages.sortedBy { it.date }
    }

    @SuppressLint("Range")
    fun getMmsAddresses(context: Context, mmsId: String): Map<Int, List<String>> {
        val addresses = mutableMapOf<Int, MutableList<String>>()
        val uri = Uri.parse("content://mms/$mmsId/addr")
        val projection = arrayOf("address", "type")
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val addressIndex = cursor.getColumnIndex("address")
            val typeIndex = cursor.getColumnIndex("type")
            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex) ?: ""
                val type = cursor.getInt(typeIndex)
                if (address.isNotEmpty() && address != "insert-address-token") {
                    if (!addresses.containsKey(type)) {
                        addresses[type] = mutableListOf()
                    }
                    addresses[type]?.add(address)
                }
            }
        }
        return addresses
    }

    fun getMmsDisplayAddress(context: Context, mmsId: String, msgBox: Int): String {
        val addrMap = getMmsAddresses(context, mmsId)
        return if (msgBox == 1) {
            // Inbox: Get FROM (137)
            addrMap[137]?.firstOrNull() ?: ""
        } else {
            // Sent/Outbox/Failed: Get TO (151)
            addrMap[151]?.joinToString(", ") ?: ""
        }
    }

    fun getMmsText(context: Context, mmsId: String): String {
        val selection = "mid=$mmsId AND ct='text/plain'"
        val uri = Uri.parse("content://mms/part")
        context.contentResolver.query(uri, arrayOf("text"), selection, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0) ?: ""
            }
        }
        return ""
    }

    private fun getMmsImage(context: Context, mmsId: String): Uri? {
        val selection = "mid=$mmsId AND ct LIKE 'image/%'"
        val uri = Uri.parse("content://mms/part")
        context.contentResolver.query(uri, arrayOf("_id"), selection, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val partId = cursor.getString(0)
                return Uri.parse("content://mms/part/$partId")
            }
        }
        return null
    }

    fun deleteConversation(context: Context, addresses: List<String>): Int {
        if (addresses.isEmpty()) return 0
        var deletedCount = 0

        // 1. Delete SMS
        val smsUri: Uri = Telephony.Sms.CONTENT_URI
        val placeholders = addresses.joinToString(",") { "?" }
        val selection = "${Telephony.Sms.ADDRESS} IN ($placeholders)"
        val selectionArgs = addresses.toTypedArray()
        deletedCount += context.contentResolver.delete(smsUri, selection, selectionArgs)

        // 2. Delete MMS (requires finding threadIds)
        val threadIds = mutableSetOf<Long>()
        addresses.forEach { addr ->
            try {
                val threadId = Telephony.Threads.getOrCreateThreadId(context, addr)
                threadIds.add(threadId)
            } catch (e: Exception) {
            }
        }

        threadIds.forEach { threadId ->
            val convUri = Uri.parse("content://mms-sms/conversations/$threadId")
            deletedCount += context.contentResolver.delete(convUri, null, null)
        }

        return deletedCount
    }

    fun markAsRead(context: Context, addresses: List<String>) {
        if (addresses.isEmpty()) return
        try {
            // 1. Mark SMS as read
            val smsUri: Uri = Telephony.Sms.CONTENT_URI
            val smsValues = ContentValues().apply { put(Telephony.Sms.READ, 1) }
            val placeholders = addresses.joinToString(",") { "?" }
            val smsSelection =
                "${Telephony.Sms.ADDRESS} IN ($placeholders) AND ${Telephony.Sms.READ} = 0"
            context.contentResolver.update(
                smsUri,
                smsValues,
                smsSelection,
                addresses.toTypedArray()
            )

            // 2. Mark MMS as read
            // MMS doesn't easily filter by address, so we find the threadIds first if possible, 
            // or just update all MMS where read=0 and they are in the inbox.
            // A more robust way is to use the conversation URI if we have threadId.

            // Try to get threadIds for these addresses
            val threadIds = mutableSetOf<Long>()
            addresses.forEach { addr ->
                try {
                    val threadId = Telephony.Threads.getOrCreateThreadId(context, addr)
                    threadIds.add(threadId)
                } catch (e: Exception) {
                }
            }

            threadIds.forEach { threadId ->
                val convUri = Uri.parse("content://mms-sms/conversations/$threadId")
                val values = ContentValues().apply { put("read", 1) }
                context.contentResolver.update(convUri, values, "read = 0", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun markThreadAsRead(context: Context, threadId: Long) {
        if (threadId <= 0) return
        try {
            val conversationValues = ContentValues().apply { put("read", 1) }
            context.contentResolver.update(
                Uri.parse("content://mms-sms/conversations/$threadId"),
                conversationValues,
                "read = 0",
                null
            )

            val smsValues = ContentValues().apply { put(Telephony.Sms.READ, 1) }
            context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                smsValues,
                "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
                arrayOf(threadId.toString())
            )

            val mmsValues = ContentValues().apply { put(Telephony.Mms.READ, 1) }
            context.contentResolver.update(
                Telephony.Mms.CONTENT_URI,
                mmsValues,
                "${Telephony.Mms.THREAD_ID} = ? AND ${Telephony.Mms.READ} = 0",
                arrayOf(threadId.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertSms(context: Context, address: String, body: String, date: Long, read: Int) {
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, date)
            put(Telephony.Sms.READ, read)
            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
        }
        try {
            context.contentResolver.insert(uri, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insertSentSms(
        context: Context,
        address: String,
        body: String,
        date: Long,
        type: Int = Telephony.Sms.MESSAGE_TYPE_SENT
    ): Uri? {
        val uri: Uri = Telephony.Sms.Sent.CONTENT_URI
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, address)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, date)
            put(Telephony.Sms.READ, 1)
            put(Telephony.Sms.TYPE, type)
        }
        return try {
            context.contentResolver.insert(uri, values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun insertSentMms(
        context: Context,
        address: String,
        body: String,
        imageUri: Uri,
        date: Long = System.currentTimeMillis(),
        msgBox: Int = 2
    ): Uri? {
        return try {

            // 1. Create MMS (pdu)
            val mmsValues = ContentValues().apply {
                put("msg_box", msgBox)
                put("date", date / 1000L)
                put("read", 1)
                put("m_type", 128) // send req
                put("v", 18)
                put("ct_t", "application/vnd.wap.multipart.related")
                put("tr_id", "T${System.currentTimeMillis()}")

                // 🔥 QUAN TRỌNG
                put("text_only", 0)
                put("m_cls", "personal")
                put("pri", 129)
                put("sub", "")
            }

            val mmsUri = context.contentResolver.insert(
                Uri.parse("content://mms"),
                mmsValues
            ) ?: return null

            val mmsId = mmsUri.lastPathSegment ?: return null

            // 2. TEXT PART
            if (body.isNotEmpty()) {
                val textValues = ContentValues().apply {
                    put("mid", mmsId)
                    put("ct", "text/plain")
                    put("text", body)
                    put("seq", 0)
                }
                context.contentResolver.insert(
                    Uri.parse("content://mms/part"),
                    textValues
                )
            }

            // 3. IMAGE PART (CHUẨN)
            val partUri = context.contentResolver.insert(
                Uri.parse("content://mms/part"),
                ContentValues().apply {
                    put("mid", mmsId)
                    put("ct", context.contentResolver.getType(imageUri) ?: "image/jpeg")
                    put("name", "image_${System.currentTimeMillis()}.jpg")
                    put("cid", "<image>")
                    put("seq", 1)
                }
            )

            partUri?.let {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    context.contentResolver.openInputStream(imageUri)?.use { input ->
                        input.copyTo(output)
                    }
                }
            }

            // 4. ADDR
            val addrValues = ContentValues().apply {
                put("address", address)
                put("type", 151) // TO
                put("charset", 106)
            }

            context.contentResolver.insert(
                Uri.parse("content://mms/$mmsId/addr"),
                addrValues
            )

            mmsUri

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun getLatestSms(context: Context, limit: Int): List<SmsMessage> {
        val allMessages = mutableListOf<SmsMessage>()
        if (!PermissionUtils.hasSmsPermission(context)) {
            return allMessages
        }

        // SMS
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI, arrayOf(
                Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY,
                Telephony.Sms.DATE, Telephony.Sms.READ, Telephony.Sms.TYPE, Telephony.Sms.THREAD_ID
            ), null, null, "date DESC LIMIT $limit"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                allMessages.add(
                    SmsMessage(
                        cursor.getString(0),
                        cursor.getString(1) ?: "",
                        cursor.getString(2) ?: "",
                        cursor.getLong(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        threadId = cursor.getString(6)
                    )
                )
            }
        }

        // MMS
        context.contentResolver.query(
            Telephony.Mms.CONTENT_URI, arrayOf(
                Telephony.Mms._ID,
                Telephony.Mms.DATE,
                Telephony.Mms.READ,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.THREAD_ID
            ), null, null, "date DESC LIMIT $limit"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val mmsId = cursor.getString(0)
                val msgBox = cursor.getInt(3)
                val displayAddress = getMmsDisplayAddress(context, mmsId, msgBox)
                if (displayAddress.isNotEmpty()) {
                    val date = cursor.getLong(1) * 1000
                    var body = getMmsText(context, mmsId)
                    if (body.isEmpty()) body = "Không có tiêu đề"

                    allMessages.add(
                        SmsMessage(
                            mmsId, displayAddress, body, date, cursor.getInt(2),
                            if (msgBox == 1) Telephony.Sms.MESSAGE_TYPE_INBOX else Telephony.Sms.MESSAGE_TYPE_SENT,
                            threadId = cursor.getString(4)
                        )
                    )
                }
            }
        }

        return allMessages.sortedByDescending { it.date }.take(limit)
    }

    fun updateSmsType(context: Context, id: String, type: Int) {
        val uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id)
        val values = ContentValues().apply {
            put(Telephony.Sms.TYPE, type)
        }
        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateMmsType(context: Context, id: String, msgBox: Int) {
        val uri = Uri.withAppendedPath(Uri.parse("content://mms"), id)
        val values = ContentValues().apply {
            put("msg_box", msgBox)
        }
        try {
            context.contentResolver.update(uri, values, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMmsMessage(
        context: Context,
        text: String,
        addresses: List<String>,
        imageUri: Uri,
        subscriptionId: Int,
        messageId: Long? = null
    ) {
        val settings = getSendMessageSettings()
        settings.subscriptionId = subscriptionId

        val transaction = Transaction(context, settings)
        val message = Message(text, addresses.toTypedArray())

        try {
            context.contentResolver.openInputStream(imageUri)?.use {
                val bytes = it.readBytes()
                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                val name = "image_${System.currentTimeMillis()}.jpg"
                message.addMedia(bytes, mimeType, name, name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val mmsSentIntent = Intent(context, MmsSentReceiver::class.java)
        mmsSentIntent.putExtra(MmsSentReceiver.EXTRA_ORIGINAL_RESENT_MESSAGE_ID, messageId)
        transaction.setExplicitBroadcastForSentMms(mmsSentIntent)

        try {
            transaction.sendNewMessage(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteMessage(context: Context, id: String, isMms: Boolean): Boolean {
        val uri = if (isMms) {
            Uri.withAppendedPath(Uri.parse("content://mms"), id)
        } else {
            Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id)
        }
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getSendMessageSettings(): Settings {
        val settings = Settings()
        settings.useSystemSending = true
        settings.deliveryReports = false
        settings.sendLongAsMms = true
        settings.sendLongAsMmsAfter = 1
        settings.group = true
        return settings
    }
}
