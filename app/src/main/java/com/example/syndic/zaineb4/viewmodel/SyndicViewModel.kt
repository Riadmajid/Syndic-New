package com.example.syndic.zaineb4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.syndic.zaineb4.data.Announcement
import com.example.syndic.zaineb4.data.Apartment
import com.example.syndic.zaineb4.data.Building
import com.example.syndic.zaineb4.data.ChatMessage
import com.example.syndic.zaineb4.data.Comment
import com.example.syndic.zaineb4.data.Expense
import com.example.syndic.zaineb4.repository.SyndicRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for Syndic app - connects UI with Repository
 */
class SyndicViewModel(private val repository: SyndicRepository) : ViewModel() {

    val appData = repository.appData
    val isAuthenticated = repository.isAuthenticated
    val userAccount = repository.userAccount
    val allUsers = repository.allUsers

    fun startListening() = repository.startListening()
    fun stopListening() = repository.stopListening()

    // Auth
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) = 
        repository.login(email, password, onResult)
        
    fun logout() = repository.logout()
    
    fun register(
        email: String,
        password: String,
        name: String,
        phone: String = "",
        apartment: String,
        building: String,
        onResult: (Boolean, String?) -> Unit
    ) = repository.register(email, password, name, phone, apartment, building, onResult)

    fun changePassword(newPwd: String, onResult: (Boolean, String?) -> Unit) = 
        repository.changePassword(newPwd, onResult)

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) =
        repository.resetPassword(email, onResult)

    fun approveUser(uid: String) = repository.approveUser(uid)
    fun makeAdmin(uid: String) = repository.makeAdmin(uid)
    fun deleteUser(uid: String) = repository.deleteUser(uid)

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

    fun savePaymentWithReceipt(
        apartmentId: Long,
        year: Int,
        month: Int,
        amount: Double,
        base64Image: String? = null,
        removeReceipt: Boolean = false,
        onComplete: () -> Unit = {}
    ) = repository.savePaymentWithReceipt(apartmentId, year, month, amount, base64Image, removeReceipt, onComplete)

    fun clearPayment(apartmentId: Long, year: Int, month: Int) =
        repository.clearPayment(apartmentId, year, month)

    fun getPaymentAmount(apartmentId: Long, year: Int, month: Int): Double =
        repository.getPaymentAmount(apartmentId, year, month)

    fun getPaymentReceiptImage(apartmentId: Long, year: Int, month: Int, onResult: (String?) -> Unit) =
        repository.getPaymentReceiptImage(apartmentId, year, month, onResult)

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
    fun likeChatMessage(messageId: Long) = repository.likeChatMessage(messageId)
    fun dislikeChatMessage(messageId: Long) = repository.dislikeChatMessage(messageId)
    fun addChatReply(messageId: Long, reply: Comment) = repository.addChatReply(messageId, reply)
    fun deleteChatReply(messageId: Long, replyId: Long) = repository.deleteChatReply(messageId, replyId)

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

