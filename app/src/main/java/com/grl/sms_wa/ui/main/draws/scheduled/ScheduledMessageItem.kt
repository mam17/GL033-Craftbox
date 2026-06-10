package com.grl.sms_wa.ui.main.draws.scheduled

import android.graphics.Bitmap

data class ScheduledMessageItem(
    val id: String,
    val address: String,
    val displayName: String,
    val hasContactName: Boolean,
    val photo: Bitmap?,
    val body: String,
    val scheduledTime: Long
)
