package com.example.syndic.repository

import com.example.syndic.data.Announcement
import com.example.syndic.data.Apartment
import com.example.syndic.data.AppData
import com.example.syndic.data.Building
import com.example.syndic.data.ChatMessage
import com.example.syndic.data.Comment
import com.example.syndic.data.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.util.Log
import com.example.syndic.utils.NotificationHelper

private const val TAG = "SyndicRepo"

/**
 * Firebase Firestore repository for Syndic app
 */
class SyndicRepository {

    private val db = FirebaseFirestore.getInstance()
    private val mainDocRef = db.collection("syndic_data").document("main")
    private val receiptsCollection = db.collection("syndic_receipts")
    private val announcementsImagesCollection = db.collection("syndic_announcements_images")

    private val _appData = MutableStateFlow(AppData())
    val appData: StateFlow<AppData> = _appData

    private var listenerRegistration: ListenerRegistration? = null
    private var appContext: Context? = null
    private var isInitialLoad = true
    private var lastAnnouncementId: Long? = null

    private var _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    fun startListening() {
        listenerRegistration = mainDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error listening to Firestore: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.toObject(AppData::class.java) ?: AppData()
                _appData.value = data
                Log.d(TAG, "Data loaded from Firestore: ${data.expenses.size} expenses")

                // Check for new announcements
                if (!isInitialLoad) {
                    val latestAnnouncement = data.announcements.maxByOrNull { it.id }
                    if (latestAnnouncement != null && (lastAnnouncementId == null || latestAnnouncement.id > lastAnnouncementId!!)) {
                        appContext?.let { ctx ->
                            NotificationHelper.showNewAnnouncementNotification(ctx, latestAnnouncement.title, latestAnnouncement.msg)
                        }
                        lastAnnouncementId = latestAnnouncement.id
                    }
                } else {
                    isInitialLoad = false
                    lastAnnouncementId = data.announcements.maxByOrNull { it.id }?.id
                }
            } else {
                // Create initial document if it doesn't exist
                Log.d(TAG, "No data found, creating initial doc")
                mainDocRef.set(AppData())
            }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
    }

    fun saveData(data: AppData) {
        mainDocRef.set(data)
            .addOnSuccessListener { Log.d(TAG, "Main data saved successfully") }
            .addOnFailureListener { e -> Log.e(TAG, "Error saving main data", e) }
    }

    // Authentication
    fun verifyPassword(password: String): Boolean {
        val savedPwd = _appData.value.password
        return if (password == savedPwd) {
            _isAuthenticated.value = true
            true
        } else {
            false
        }
    }

    fun logout() {
        _isAuthenticated.value = false
    }

    fun changePassword(oldPwd: String, newPwd: String): Boolean {
        return if (oldPwd == _appData.value.password) {
            val updated = _appData.value.copy(password = newPwd)
            saveData(updated)
            true
        } else {
            false
        }
    }

    // Buildings
    fun addBuilding(name: String) {
        val current = _appData.value
        val newBuilding = Building(
            id = System.currentTimeMillis(),
            name = name
        )
        val updated = current.copy(buildings = current.buildings + newBuilding)
        saveData(updated)
    }

    fun updateBuilding(building: Building) {
        val current = _appData.value
        val updated = current.copy(
            buildings = current.buildings.map { if (it.id == building.id) building else it }
        )
        saveData(updated)
    }

    fun deleteBuilding(buildingId: Long) {
        val current = _appData.value
        // Delete all apartments in this building
        val apartmentsToDelete = current.apartments.filter { it.buildingId == buildingId }
        val apartmentIds = apartmentsToDelete.map { it.id.toString() }

        // Remove payments for these apartments
        val updatedPayments = current.payments.filterNot { (key, _) ->
            apartmentIds.any { id -> key.startsWith("${id}_") }
        }

        val updated = current.copy(
            buildings = current.buildings.filter { it.id != buildingId },
            apartments = current.apartments.filter { it.buildingId != buildingId },
            payments = updatedPayments
        )
        saveData(updated)
    }

    // Apartments
    fun addApartment(buildingId: Long, name: String) {
        val current = _appData.value
        val newApartment = Apartment(
            id = System.currentTimeMillis(),
            buildingId = buildingId,
            name = name
        )
        val updated = current.copy(apartments = current.apartments + newApartment)
        saveData(updated)
    }

    fun updateApartment(apartment: Apartment) {
        val current = _appData.value
        val updated = current.copy(
            apartments = current.apartments.map { if (it.id == apartment.id) apartment else it }
        )
        saveData(updated)
    }

    fun deleteApartment(apartmentId: Long) {
        val current = _appData.value
        // Remove payments for this apartment
        val updatedPayments = current.payments.filterNot { (key, _) ->
            key.startsWith("${apartmentId}_")
        }
        val updated = current.copy(
            apartments = current.apartments.filter { it.id != apartmentId },
            payments = updatedPayments
        )
        saveData(updated)
    }

    // Payments
    fun savePayment(apartmentId: Long, year: Int, month: Int, amount: Double) {
        val current = _appData.value
        val key = "${apartmentId}_${year}_${month}"
        val updatedPayments = current.payments.toMutableMap()
        updatedPayments[key] = amount

        // Update apartment last paid time
        val updatedApartments = current.apartments.map {
            if (it.id == apartmentId) it.copy(lastPaidTime = System.currentTimeMillis()) else it
        }

        val updated = current.copy(
            payments = updatedPayments,
            apartments = updatedApartments
        )
        saveData(updated)
    }

    fun clearPayment(apartmentId: Long, year: Int, month: Int) {
        val current = _appData.value
        val key = "${apartmentId}_${year}_${month}"
        val updatedPayments = current.payments.toMutableMap()
        updatedPayments.remove(key)
        val updated = current.copy(payments = updatedPayments)
        saveData(updated)
    }

    fun getPaymentAmount(apartmentId: Long, year: Int, month: Int): Double {
        val key = "${apartmentId}_${year}_${month}"
        return _appData.value.payments[key] ?: 0.0
    }

    // Expenses
    fun addExpense(expense: Expense) {
        val current = _appData.value
        val updated = current.copy(expenses = current.expenses + expense)
        saveData(updated)
    }

    fun deleteExpense(expenseId: Long) {
        // Delete receipt image if exists
        receiptsCollection.document(expenseId.toString()).delete()

        val current = _appData.value
        val updated = current.copy(expenses = current.expenses.filter { it.id != expenseId })
        saveData(updated)
    }

    fun addExpenseWithReceipt(expense: Expense, base64Image: String?, onComplete: () -> Unit = {}) {
        val current = _appData.value
        val updated = current.copy(expenses = current.expenses + expense)
        saveData(updated)

        if (base64Image != null) {
            receiptsCollection.document(expense.id.toString())
                .set(mapOf("image" to base64Image))
                .addOnSuccessListener { 
                    Log.d(TAG, "Receipt image saved successfully for ${expense.id}")
                    onComplete() 
                }
                .addOnFailureListener { e -> 
                    Log.e(TAG, "Error saving receipt image for ${expense.id}", e)
                    onComplete() 
                }
        } else {
            onComplete()
        }
    }

    fun saveReceiptImage(expenseId: Long, base64Image: String, onComplete: () -> Unit) {
        val current = _appData.value
        val updatedExpenses = current.expenses.map {
            if (it.id == expenseId) it.copy(hasReceipt = true) else it
        }
        val updated = current.copy(expenses = updatedExpenses)
        saveData(updated)

        receiptsCollection.document(expenseId.toString())
            .set(mapOf("image" to base64Image))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }

    // Announcements
    fun addAnnouncement(announcement: Announcement) {
        val current = _appData.value
        val updated = current.copy(
            announcements = listOf(announcement) + current.announcements
        )
        saveData(updated)
    }

    fun addAnnouncementWithImage(announcement: Announcement, base64Image: String?, onComplete: () -> Unit = {}) {
        val current = _appData.value
        val updated = current.copy(
            announcements = listOf(announcement) + current.announcements
        )
        saveData(updated)

        if (base64Image != null) {
            announcementsImagesCollection.document(announcement.id.toString())
                .set(mapOf("image" to base64Image))
                .addOnSuccessListener {
                    Log.d(TAG, "Announcement image saved successfully for ${announcement.id}")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving announcement image for ${announcement.id}", e)
                    onComplete()
                }
        } else {
            onComplete()
        }
    }

    fun deleteAnnouncement(announcementId: Long) {
        // Delete image if exists
        announcementsImagesCollection.document(announcementId.toString()).delete()

        val current = _appData.value
        val updated = current.copy(
            announcements = current.announcements.filter { it.id != announcementId }
        )
        saveData(updated)
    }

    fun addComment(announcementId: Long, comment: Comment) {
        val current = _appData.value
        val updatedAnnouncements = current.announcements.map { ann ->
            if (ann.id == announcementId) {
                ann.copy(comments = ann.comments + comment)
            } else {
                ann
            }
        }
        val updated = current.copy(announcements = updatedAnnouncements)
        saveData(updated)
    }

    fun deleteComment(announcementId: Long, commentId: Long) {
        val current = _appData.value
        val updatedAnnouncements = current.announcements.map { ann ->
            if (ann.id == announcementId) {
                ann.copy(comments = ann.comments.filter { it.id != commentId })
            } else {
                ann
            }
        }
        val updated = current.copy(announcements = updatedAnnouncements)
        saveData(updated)
    }

    fun saveAnnouncementImage(announcementId: Long, base64Image: String, onComplete: () -> Unit) {
        announcementsImagesCollection.document(announcementId.toString())
            .set(mapOf("image" to base64Image))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }

    // Chat
    fun sendChatMessage(message: ChatMessage) {
        val current = _appData.value
        var updatedChat = current.chat + message
        // Keep only last 100 messages
        if (updatedChat.size > 100) {
            updatedChat = updatedChat.takeLast(100)
        }
        val updated = current.copy(chat = updatedChat)
        saveData(updated)
    }

    fun deleteChatMessage(messageId: Long) {
        val current = _appData.value
        val updated = current.copy(chat = current.chat.filter { it.id != messageId })
        saveData(updated)
    }

    // Statistics
    fun calculateStats(month: Int?, year: Int): Triple<Double, Double, Double> {
        val current = _appData.value
        val validApartmentIds = current.apartments.map { it.id.toString() }.toSet()

        // Calculate total incomes
        var totalIn = 0.0
        var filteredIn = 0.0

        current.payments.forEach { (key, amount) ->
            val parts = key.split("_")
            if (parts.size == 3) {
                val aptId = parts[0]
                val payYear = parts[1].toIntOrNull()
                val payMonth = parts[2].toIntOrNull()

                if (validApartmentIds.contains(aptId) && payYear != null && payMonth != null) {
                    totalIn += amount
                    if (month == null) {
                        if (payYear == year) filteredIn += amount
                    } else {
                        if (payYear == year && payMonth == month) filteredIn += amount
                    }
                }
            }
        }

        // Calculate expenses
        var totalOut = 0.0
        var filteredOut = 0.0

        current.expenses.forEach { expense ->
            val dateParts = expense.date.split("-")
            if (dateParts.size >= 3) {
                val expYear = dateParts[0].toIntOrNull()
                val expMonth = dateParts[1].toIntOrNull()

                if (expYear != null && expMonth != null) {
                    totalOut += expense.amount
                    if (month == null) {
                        if (expYear == year) filteredOut += expense.amount
                    } else {
                        if (expYear == year && expMonth == month) filteredOut += expense.amount
                    }
                }
            }
        }

        return Triple(filteredIn, filteredOut, totalIn - totalOut)
    }
}
