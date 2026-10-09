package com.example.syndic.zaineb4.data

/**
 * Comment/تعليق data model for announcements
 */
data class Comment(
    var id: Long = 0L,
    var name: String = "",
    var text: String = "",
    var date: String = ""
) {
    constructor() : this(0L, "", "", "")
}

