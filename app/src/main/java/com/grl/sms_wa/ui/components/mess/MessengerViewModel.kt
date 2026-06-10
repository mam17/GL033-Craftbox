package com.grl.sms_wa.ui.components.mess

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grl.sms_wa.R
import com.grl.sms_wa.data.model.SmsMessage
import com.grl.sms_wa.data.repository.SmsRepository
import com.grl.sms_wa.utils.AppEx.isSameDay
import com.grl.sms_wa.utils.Constant
import com.grl.sms_wa.utils.ImageUtils
import com.grl.sms_wa.utils.NetworkUtil
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.notification.NotificationSMS
import com.grl.sms_wa.receiver.SmsSentReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class MessengerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val smsRepository: SmsRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<MessageItem>>()
    val messages: LiveData<List<MessageItem>> get() = _messages

    private val _isBlocked = MutableLiveData<Boolean>()
    val isBlocked: LiveData<Boolean> get() = _isBlocked

    private val _isArchived = MutableLiveData<Boolean>()
    val isArchived: LiveData<Boolean> get() = _isArchived

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> get() = _notificationsEnabled

    private val _hasPassword = MutableLiveData<Boolean>()
    val hasPassword: LiveData<Boolean> get() = _hasPassword

    private val _background = MutableLiveData<String>()
    val background: LiveData<String> get() = _background

    private val _conversationDeleted = MutableLiveData<Boolean>()
    val conversationDeleted: LiveData<Boolean> get() = _conversationDeleted

    var address: String = ""
    var recipients: List<String> = emptyList() // The actual recipients for sending
    var fetchAddresses: List<String> = emptyList() // Addresses for querying (includes variants)
    private var threadId: Long = -1L
    private lateinit var spManager: SpManager
    private var contentObserver: ContentObserver? = null
    private var mContext: Context? = null
    private var refreshJob: Job? = null

    fun init(context: Context, address: String, groupAddresses: List<String>, threadId: Long = -1L) {
        this.mContext = context.applicationContext
        this.address = address
        this.threadId = threadId
        this.recipients = if (groupAddresses.isEmpty()) listOf(address) else groupAddresses

        val allFetch = mutableListOf<String>()
        for (addr in recipients) {
            allFetch.add(addr)
            if (addr.startsWith("0")) {
                allFetch.add("+84" + addr.substring(1))
            } else if (addr.startsWith("+84")) {
                allFetch.add("0" + addr.substring(3))
            }
        }
        this.fetchAddresses = allFetch.distinct()

        this.spManager = SpManager.get(context)
        _isBlocked.value = spManager.isBlocked(address)
        _isArchived.value = spManager.isArchived(address)
        _notificationsEnabled.value = spManager.areNotificationsEnabled(address)
        _hasPassword.value = spManager.hasConversationPassword(address)
        _background.value = spManager.getMessageBackground(address)
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            smsRepository.refreshFlow.collect {
                mContext?.let { appContext -> fetchMessages(appContext) }
            }
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (contentObserver == null) {
                contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean, uri: Uri?) {
                        super.onChange(selfChange, uri)
                        mContext?.let {
                            smsRepository.markAsRead(it, this@MessengerViewModel.fetchAddresses)
                            smsRepository.markThreadAsRead(it, this@MessengerViewModel.threadId)
                            cancelConversationNotifications(it)
                            fetchMessages(it)
                        }
                    }
                }
                context.applicationContext.contentResolver.registerContentObserver(
                    Telephony.Sms.CONTENT_URI,
                    true,
                    contentObserver!!
                )
                context.applicationContext.contentResolver.registerContentObserver(
                    "content://mms-sms/".toUri(),
                    true,
                    contentObserver!!
                )
            }

            smsRepository.markAsRead(context, this.fetchAddresses)
            smsRepository.markThreadAsRead(context, threadId)
            cancelConversationNotifications(context)
            fetchMessages(context)
        }
    }

    override fun onCleared() {
        super.onCleared()
        contentObserver?.let {
            mContext?.contentResolver?.unregisterContentObserver(it)
        }
        contentObserver = null
    }

    fun fetchMessages(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                val rawMessages = smsRepository.getMessagesByAddresses(context, fetchAddresses)
                val list = mutableListOf<MessageItem>()
                var lastDate: Long? = null
                for (message in rawMessages) {
                    if (lastDate == null || !message.date.isSameDay(lastDate)) {
                        list.add(MessageItem.DateHeader(message.date))
                        lastDate = message.date
                    }
                    list.add(MessageItem.MessageContent(message))
                }
                list
            }
            _messages.postValue(items)
        }
    }

    private fun cancelConversationNotifications(context: Context) {
        NotificationSMS.cancelNotification(context, address)
        recipients.forEach { NotificationSMS.cancelNotification(context, it) }
        fetchAddresses.forEach { NotificationSMS.cancelNotification(context, it) }
    }

    fun toggleBlock() {
        val newState = !(_isBlocked.value ?: false)
        spManager.setBlocked(address, newState)
        _isBlocked.value = newState

        mContext?.let {
            val resId = if (newState) R.string.txt_blocked else R.string.txt_unblocked
            makeText(it, it.getString(resId), Toast.LENGTH_SHORT).show()
        }
        smsRepository.notifyDataChanged()
    }

    fun toggleArchive() {
        val newState = !(_isArchived.value ?: false)
        spManager.setArchived(address, newState)
        _isArchived.value = newState
        smsRepository.notifyDataChanged()
    }

    fun archiveConversation() {
        spManager.setArchived(address, true)
        _isArchived.value = true
        smsRepository.notifyDataChanged()
    }

    fun toggleNotifications() {
        val newState = !(_notificationsEnabled.value ?: true)
        spManager.setNotificationsEnabled(address, newState)
        _notificationsEnabled.value = newState
        if (!newState) {
            mContext?.let { cancelConversationNotifications(it) }
        }
    }

    fun setConversationPassword(password: String) {
        spManager.setConversationPassword(address, password)
        _hasPassword.value = true
    }

    fun removeConversationPassword() {
        spManager.setConversationPassword(address, null)
        _hasPassword.value = false
    }

    fun isConversationPassword(password: String): Boolean {
        return spManager.getConversationPassword(address) == password
    }

    fun blockConversation() {
        spManager.setBlocked(address, true)
        _isBlocked.value = true
        mContext?.let {
            Toast.makeText(it, it.getString(R.string.txt_blocked), Toast.LENGTH_SHORT).show()
        }
        smsRepository.notifyDataChanged()
    }

    fun deleteConversation(context: Context) {
        smsRepository.deleteConversation(context, fetchAddresses)
        smsRepository.notifyDataChanged()
        _conversationDeleted.postValue(true)
    }

    fun sendSms(context: Context, body: String, subscriptionId: Int = -1) {
        if (body.isBlank()) return
        sendMessage(context, body, null, subscriptionId)
    }

    fun sendMms(context: Context, mediaUri: Uri, body: String = "", subscriptionId: Int = -1) {
        if (!NetworkUtil.isMobileDataEnabled(context)) {
            makeText(
                context,
                context.getString(R.string.txt_mms_requires_data),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        viewModelScope.launch {
            val internalUri = withContext(Dispatchers.IO) {
                ImageUtils.compressImageToInternal(context, mediaUri, 300)
            }
            if (internalUri != null) {
                withContext(Dispatchers.IO) {
                    smsRepository.sendMmsMessage(context, body, recipients, internalUri, subscriptionId)
                }
                fetchMessages(context)
            } else {
                makeText(
                    context,
                    context.getString(R.string.txt_failed_to_process_image),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendMessage(
        context: Context,
        body: String,
        mediaUri: String?,
        subscriptionId: Int
    ) {
        viewModelScope.launch {
            try {
                if (mediaUri == null) {
                    val date = System.currentTimeMillis()
                    withContext(Dispatchers.IO) {
                        val smsManager = getSmsManager(context, subscriptionId)
                        recipients.forEach { recipient ->
                            val insertedUri = smsRepository.insertSentSms(
                                context,
                                recipient,
                                body,
                                date
                            )

                            val messageId = insertedUri?.lastPathSegment ?: ""

                            val sentIntent = PendingIntent.getBroadcast(
                                context,
                                messageId.toIntOrNull() ?: System.currentTimeMillis().toInt(),
                                Intent(context, SmsSentReceiver::class.java).apply {
                                    action = Constant.ACTION_SMS_SENT
                                    putExtra(Constant.EXTRA_MESSAGE_ID, messageId)
                                },
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            smsManager.sendTextMessage(recipient, null, body, sentIntent, null)
                        }
                    }
                    fetchMessages(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    makeText(
                        context,
                        context.getString(R.string.txt_failed_to_send),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getSmsManager(context: Context, subscriptionId: Int): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val sm = context.getSystemService(SmsManager::class.java)
            if (subscriptionId != -1) sm.createForSubscriptionId(subscriptionId) else sm
        } else {
            @Suppress("DEPRECATION")
            if (subscriptionId != -1) SmsManager.getSmsManagerForSubscriptionId(subscriptionId) else SmsManager.getDefault()
        }
    }

    fun retrySendMessage(context: Context, message: SmsMessage, subscriptionId: Int = -1) {
        viewModelScope.launch {
            val isMms = message.mediaUri != null
            smsRepository.deleteMessage(context, message.id, isMms)
            if (isMms) {
                sendMms(context, message.mediaUri.toUri(), message.body, subscriptionId)
            } else {
                sendSms(context, message.body, subscriptionId)
            }
        }
    }

    fun handleSmsSentResult(context: Context, id: String, resultCode: Int) {
        if (resultCode == android.app.Activity.RESULT_OK) {
            smsRepository.updateSmsType(context, id, Telephony.Sms.MESSAGE_TYPE_SENT)
        } else {
            smsRepository.updateSmsType(context, id, Telephony.Sms.MESSAGE_TYPE_FAILED)
            makeText(
                context,
                context.getString(R.string.txt_failed_to_send),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        fetchMessages(context)
    }
}
