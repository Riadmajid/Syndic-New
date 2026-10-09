package com.example.syndic.zaineb4.data
import com.google.firebase.firestore.PropertyName

/**
 * Chat message/رسالة دردشة data model
 */
data class ChatMessage(
    val id: Long = 0L,
    val name: String = "",
    val msg: String = "",
    val time: String = "",
    val likes: Int = 0,
    val dislikes: Int = 0,
    val replies: List<Comment> = emptyList(),
    @get:PropertyName("isPrivate")
    val isPrivate: Boolean = false,
    val senderUid: String = ""
) {
    constructor() : this(0L, "", "", "", 0, 0, emptyList(), false, "")
}
