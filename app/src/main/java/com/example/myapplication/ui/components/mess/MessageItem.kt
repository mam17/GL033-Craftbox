package com.example.myapplication.ui.components.mess

import com.example.myapplication.data.model.SmsMessage


sealed class MessageItem {
    data class DateHeader(val date: Long) : MessageItem()
    data class MessageContent(val message: SmsMessage) : MessageItem()
}
