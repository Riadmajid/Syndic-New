package com.example.syndic.zaineb4.data

/**
 * Main app data container - mirrors the JavaScript data structure
 */
data class AppData(
    val buildings: List<Building> = emptyList(),
    val apartments: List<Apartment> = emptyList(),
    val payments: Map<String, Double> = emptyMap(),
    val expenses: List<Expense> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val chat: List<ChatMessage> = emptyList(),
    val paymentReceipts: Map<String, Boolean> = emptyMap()
) {
    constructor() : this(emptyList(), emptyList(), emptyMap(), emptyList(), emptyList(), emptyList(), emptyMap())
}

