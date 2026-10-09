package com.example.syndic.zaineb4.data

/**
 * Apartment/شقة data model
 */
data class Apartment(
    val id: Long = 0L,
    val buildingId: Long = 0L,
    val name: String = "",
    val residentName: String = "",
    val residentPhone: String = "",
    val lastPaidTime: Long = 0L,
    val lastTotal: Double = 0.0
) {
    constructor() : this(0L, 0L, "", "", "", 0L, 0.0)
}

