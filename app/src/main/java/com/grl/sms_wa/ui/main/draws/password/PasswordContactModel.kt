package com.grl.sms_wa.ui.main.draws.password

import android.graphics.Bitmap

data class PasswordContactModel(
    val address: String,
    val displayName: String,
    val hasContactName: Boolean,
    val photo: Bitmap?
)
