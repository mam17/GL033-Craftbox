package com.example.myapplication.ui.main.draws.archived

import android.graphics.Bitmap

data class ArchivedContactModel(
    val address: String,
    val displayName: String,
    val hasContactName: Boolean,
    val photo: Bitmap?
)
