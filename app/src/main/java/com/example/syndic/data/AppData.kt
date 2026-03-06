package com.example.syndic.data

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
    val password: String = "0000"
) {
    constructor() : this(emptyList(), emptyList(), emptyMap(), emptyList(), emptyList(), emptyList(), "0000")
}
