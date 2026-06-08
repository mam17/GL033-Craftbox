package com.example.myapplication.sms_helper

data class SmsMessageModel(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val dateMillis: Long,
    val type: SmsType,
    val isRead: Boolean,
    val contactName: String? = null,
    val contactPhotoUri: String? = null
)

enum class SmsType {
    INBOX,
    SENT,
    DRAFT,
    OUTBOX,
    FAILED,
    QUEUED,
    UNKNOWN
}
