package com.example.myapplication.data.model

import android.net.Uri

data class Attachment(
    var id: Long?,
    var messageId: Long,
    var uriString: String,
    var mimetype: String,
    var width: Int,
    var height: Int,
    var filename: String
) {
    fun getUri(): Uri = Uri.parse(uriString)
}
