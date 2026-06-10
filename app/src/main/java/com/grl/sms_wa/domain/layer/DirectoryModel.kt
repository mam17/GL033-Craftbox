package com.grl.sms_wa.domain.layer

import android.graphics.Bitmap

data class DirectoryModel (
    val strName: String,
    val strPhone: String,
    val photo: Bitmap?,
)