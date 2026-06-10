package com.grl.sms_wa.ui.components.mess

import com.grl.sms_wa.data.model.SmsMessage


sealed class MessageItem {
    data class DateHeader(val date: Long) : MessageItem()
    data class MessageContent(val message: SmsMessage) : MessageItem()
}
