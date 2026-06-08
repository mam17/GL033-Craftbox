package com.example.myapplication.data.model

data class ScheduledMessage(
    val id: String,
    val address: String,
    val contactName: String?,
    val body: String,
    val scheduledTime: Long,
    val subId: Int = -1,
    val isSent: Boolean = false
)
