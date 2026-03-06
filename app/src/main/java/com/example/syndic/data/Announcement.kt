package com.example.syndic.data

/**
 * Announcement/إعلان data model
 */
data class Announcement(
    val id: Long = 0L,
    val title: String = "",
    val msg: String = "",
    val date: String = "",
    val hasImage: Boolean = false,
    val comments: List<Comment> = emptyList()
) {
    constructor() : this(0L, "", "", "", false, emptyList())
}
