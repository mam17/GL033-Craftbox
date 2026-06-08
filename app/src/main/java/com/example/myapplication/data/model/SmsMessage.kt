package com.example.myapplication.data.model

data class SmsMessage(
    val id: String,
    val address: String,
    val body: String,
    val date: Long,
    val read: Int,
    val type: Int,
    val threadId: String? = null,
    val groupAddresses: List<String> = listOf(),
    val contactName: String? = null,
    val mediaUri: String? = null
) {
    val finalGroupAddresses: List<String>
        get() = groupAddresses.ifEmpty { listOf(address) }
}
