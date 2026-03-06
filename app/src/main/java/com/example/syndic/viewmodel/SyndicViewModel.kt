package com.example.syndic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.syndic.data.Announcement
import com.example.syndic.data.Apartment
import com.example.syndic.data.Building
import com.example.syndic.data.ChatMessage
import com.example.syndic.data.Comment
import com.example.syndic.data.Expense
import com.example.syndic.repository.SyndicRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for Syndic app - connects UI with Repository
 */
class SyndicViewModel(private val repository: SyndicRepository) : ViewModel() {

    val appData = repository.appData
    val isAuthenticated = repository.isAuthenticated

    fun startListening() = repository.startListening()
    fun stopListening() = repository.stopListening()

    // Auth
    fun verifyPassword(password: String): Boolean = repository.verifyPassword(password)
    fun logout() = repository.logout()
    fun changePassword(oldPwd: String, newPwd: String): Boolean = repository.changePassword(oldPwd, newPwd)

    // Buildings
    fun addBuilding(name: String) = repository.addBuilding(name)
    fun updateBuilding(building: Building) = repository.updateBuilding(building)
    fun deleteBuilding(buildingId: Long) = repository.deleteBuilding(buildingId)

    // Apartments
    fun addApartment(buildingId: Long, name: String) = repository.addApartment(buildingId, name)
    fun updateApartment(apartment: Apartment) = repository.updateApartment(apartment)
    fun deleteApartment(apartmentId: Long) = repository.deleteApartment(apartmentId)

    // Payments
    fun savePayment(apartmentId: Long, year: Int, month: Int, amount: Double) =
        repository.savePayment(apartmentId, year, month, amount)

    fun clearPayment(apartmentId: Long, year: Int, month: Int) =
        repository.clearPayment(apartmentId, year, month)

    fun getPaymentAmount(apartmentId: Long, year: Int, month: Int): Double =
        repository.getPaymentAmount(apartmentId, year, month)

    // Expenses
    fun addExpense(expense: Expense) = repository.addExpense(expense)
    fun addExpenseWithReceipt(expense: Expense, base64Image: String?, onComplete: () -> Unit = {}) =
        repository.addExpenseWithReceipt(expense, base64Image, onComplete)
    fun deleteExpense(expenseId: Long) = repository.deleteExpense(expenseId)
    fun saveReceiptImage(expenseId: Long, base64Image: String, onComplete: () -> Unit) =
        repository.saveReceiptImage(expenseId, base64Image, onComplete)

    // Announcements
    fun addAnnouncement(announcement: Announcement) = repository.addAnnouncement(announcement)
    fun addAnnouncementWithImage(announcement: Announcement, base64Image: String?, onComplete: () -> Unit = {}) =
        repository.addAnnouncementWithImage(announcement, base64Image, onComplete)
    fun deleteAnnouncement(announcementId: Long) = repository.deleteAnnouncement(announcementId)
    fun addComment(announcementId: Long, comment: Comment) = repository.addComment(announcementId, comment)
    fun deleteComment(announcementId: Long, commentId: Long) = repository.deleteComment(announcementId, commentId)
    fun saveAnnouncementImage(announcementId: Long, base64Image: String, onComplete: () -> Unit) =
        repository.saveAnnouncementImage(announcementId, base64Image, onComplete)

    // Chat
    fun sendChatMessage(message: ChatMessage) = repository.sendChatMessage(message)
    fun deleteChatMessage(messageId: Long) = repository.deleteChatMessage(messageId)

    // Stats
    fun calculateStats(month: Int?, year: Int): Triple<Double, Double, Double> =
        repository.calculateStats(month, year)

    companion object {
        fun provideFactory(repository: SyndicRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SyndicViewModel(repository) as T
                }
            }
    }
}
