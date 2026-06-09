package com.example.myapplication.domain.layer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StickerModel(
    val name: String,
    val previewPath: String,
    val firstImagePath: String,
    val detailPaths: List<String> = emptyList(),
    var isAdded: Boolean = false
) : Parcelable
