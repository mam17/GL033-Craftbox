package com.example.myapplication.domain.layer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ThemeMessModel(
    val id: Int,

    val pathThemePreview: String,

    val pathBG: String,
    val isUnlockBG: Boolean = false,

    val pathAvt: String,
    val isUnlockAvt: Boolean = false,

    val pathBubbleSent: String,
    val isUnlockSent: Boolean = false,

    val pathBubbleReceived: String,
    val isUnlockReceived: Boolean = false,

    val pathEnterChat: String,
    val isUnlockChat: Boolean = false,

    val isUnlockAll: Boolean = false,

    val typeBubble: Int,
    val enableStroke: Boolean = false,
    val colorStroke: String,
    val widthStroke: Int,

    val font: String,

    val colMain: String,

    val colStrokeBBReceived: String,
    val colTextBBReceived: String,
    val colBGBBReceived: String,

    val colStrokeBBSent: String,
    val colTextBBSent: String,
    val colBGBBSent: String,

    val colBGEnterChat: String,
    val colTextEnterChat: String,
) : Parcelable


