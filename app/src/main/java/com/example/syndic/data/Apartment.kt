package com.example.syndic.data

/**
 * Apartment/شقة data model
 */
data class Apartment(
    val id: Long = 0L,
    val buildingId: Long = 0L,
    val name: String = "",
    val residentName: String = "",
    val residentPhone: String = "",
    val lastPaidTime: Long = 0L
) {
    constructor() : this(0L, 0L, "", "", "", 0L)
}
