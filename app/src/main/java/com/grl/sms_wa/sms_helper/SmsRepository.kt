package com.grl.sms_wa.sms_helper

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class SmsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val smsReader = SmsReader(appContext)

    fun observeAllMessages(): Flow<List<SmsMessageModel>> {
        return callbackFlow {
            var loadJob: Job? = null

            fun loadMessages() {
                loadJob?.cancel()
                loadJob = launch {
                    send(smsReader.getAllMessages())
                }
            }

            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    loadMessages()
                }

                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    loadMessages()
                }
            }

            appContext.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer
            )
            loadMessages()

            awaitClose {
                loadJob?.cancel()
                appContext.contentResolver.unregisterContentObserver(observer)
            }
        }.conflate()
    }

    suspend fun getLatestMessagesByThread(): List<SmsMessageModel> {
        return smsReader.getLatestMessagesByThread()
    }

    fun observeLatestMessagesByThread(): Flow<List<SmsMessageModel>> {
        return callbackFlow {
            var loadJob: Job? = null

            fun loadMessages() {
                loadJob?.cancel()
                loadJob = launch {
                    send(smsReader.getLatestMessagesByThread())
                }
            }

            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    loadMessages()
                }

                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    loadMessages()
                }
            }

            appContext.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer
            )
            loadMessages()

            awaitClose {
                loadJob?.cancel()
                appContext.contentResolver.unregisterContentObserver(observer)
            }
        }.conflate()
    }

    fun observeMessagesByThread(threadId: Long): Flow<List<SmsMessageModel>> {
        return callbackFlow {
            var loadJob: Job? = null

            fun loadMessages() {
                loadJob?.cancel()
                loadJob = launch {
                    send(smsReader.getMessagesByThread(threadId))
                }
            }

            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    loadMessages()
                }

                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    loadMessages()
                }
            }

            appContext.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer
            )
            loadMessages()

            awaitClose {
                loadJob?.cancel()
                appContext.contentResolver.unregisterContentObserver(observer)
            }
        }.conflate()
    }
}
