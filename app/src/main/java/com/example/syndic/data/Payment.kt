package com.example.syndic.data

/**
 * Payment/أداء data model - stored in Firestore as a map
 * Key format: "{apartmentId}_{year}_{month}"
 */
data class Payment(
    val apartmentId: Long = 0L,
    val year: Int = 0,
    val month: Int = 0,
    val amount: Double = 0.0
) {
    fun getKey(): String = "${apartmentId}_${year}_${month}"
}
