package com.example.myapplication.ui.main.draws.blocked

import android.graphics.Bitmap

data class BlockedContactModel(
    val address: String,
    val displayName: String,
    val hasContactName: Boolean,
    val photo: Bitmap?
)
