package com.example.syndic.zaineb4.data

/**
 * Expense/مصروف data model
 */
data class Expense(
    val id: Long = 0L,
    val desc: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val hasReceipt: Boolean = false
) {
    constructor() : this(0L, "", 0.0, "", false)
}

