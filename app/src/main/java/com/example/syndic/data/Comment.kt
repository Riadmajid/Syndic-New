package com.example.syndic.data

/**
 * Comment/تعليق data model for announcements
 */
data class Comment(
    val id: Long = 0L,
    val name: String = "",
    val text: String = "",
    val date: String = ""
) {
    constructor() : this(0L, "", "", "")
}
