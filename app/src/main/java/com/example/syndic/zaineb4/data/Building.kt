package com.example.syndic.zaineb4.data

/**
 * Building/عمارة data model
 */
data class Building(
    val id: Long = 0L,
    val name: String = ""
) {
    constructor() : this(0L, "")
}

