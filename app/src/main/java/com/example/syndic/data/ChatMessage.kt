package com.example.syndic.data

/**
 * Chat message/رسالة دردشة data model
 */
data class ChatMessage(
    val id: Long = 0L,
    val name: String = "",
    val msg: String = "",
    val time: String = ""
) {
    constructor() : this(0L, "", "", "")
}
