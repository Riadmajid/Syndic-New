package com.example.syndic.zaineb4.repository

import com.example.syndic.zaineb4.data.Announcement
import com.example.syndic.zaineb4.data.Apartment
import com.example.syndic.zaineb4.data.AppData
import com.example.syndic.zaineb4.data.Building
import com.example.syndic.zaineb4.data.ChatMessage
import com.example.syndic.zaineb4.data.Comment
import com.example.syndic.zaineb4.data.Expense
import com.example.syndic.zaineb4.data.UserAccount
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.Context
import android.util.Log
import com.example.syndic.zaineb4.utils.NotificationHelper
import com.example.syndic.zaineb4.utils.IdGenerator
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "SyndicRepo"

/**
 * Firebase Firestore repository for Syndic app
 */
class SyndicRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val mainDocRef = db.collection("syndic_data").document("zaineb4")
    private val receiptsCollection = db.collection("syndic_receipts")
    private val paymentReceiptsCollection = db.collection("syndic_payment_receipts")
    private val announcementsImagesCollection = db.collection("syndic_announcements_images")
    private val chatCollection = db.collection("syndic_chat")
    private val announcementCollection = db.collection("syndic_announcements")



    private val _appData = MutableStateFlow(AppData())
    val appData: StateFlow<AppData> = _appData

    private var listenerRegistration: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null
    private var announcementListener: ListenerRegistration? = null

    private var appContext: Context? = null

    private var isInitialLoad = true
    private var lastAnnouncementId: Long? = null

    private var _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _userAccount = MutableStateFlow<UserAccount?>(null)
    val userAccount: StateFlow<UserAccount?> = _userAccount

    private val _allUsers = MutableStateFlow<List<UserAccount>>(emptyList())
    val allUsers: StateFlow<List<UserAccount>> = _allUsers

    private var userListener: ListenerRegistration? = null
    private var allUsersListener: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isAuthenticated.value = user != null
            if (user == null) {
                stopListening()
                stopUserListening()
                _userAccount.value = null
            } else {
                startUserListening(user.uid)
            }
        }
    }

    private fun startUserListening(uid: String) {
        userListener?.remove()
        userListener = db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to user doc: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val account = snapshot.toObject(UserAccount::class.java)
                _userAccount.value = account
                if (account?.approved == true) {
                    startListening()
                    if (account.role == "admin") {
                        startAllUsersListening()
                    } else {
                        stopAllUsersListening()
                    }
                } else {
                    stopListening()
                    stopAllUsersListening()
                }
            } else {
                // If doc doesn't exist, create it automatically (e.g., for legacy users)
                val email = auth.currentUser?.email ?: ""
                val newAccount = UserAccount(uid = uid, email = email, role = "resident", approved = false)
                db.collection("users").document(uid).set(newAccount)
                _userAccount.value = newAccount
            }
        }
    }

    private fun stopUserListening() {
        userListener?.remove()
        userListener = null
    }

    private fun startAllUsersListening() {
        if (allUsersListener != null) return
        allUsersListener = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                val users = snapshot.toObjects(UserAccount::class.java)
                _allUsers.value = users
            }
        }
    }

    private fun stopAllUsersListening() {
        allUsersListener?.remove()
        allUsersListener = null
        _allUsers.value = emptyList()
    }

    fun approveUser(uid: String) {
        // 1. Mark user as approved in Firestore using set+merge for robustness
        db.collection("users").document(uid)
            .update("approved", true)
            .addOnSuccessListener {
                Log.d(TAG, "✅ approveUser SUCCESS for uid=$uid")

                // 2. Auto-register user in the apartments/buildings list
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null && snapshot.exists()) {
                            val userAccount = snapshot.toObject(UserAccount::class.java) ?: return@addOnSuccessListener
                            val buildingName = userAccount.building.trim()
                            val apartmentName = userAccount.apartment.trim()
                            val residentName = userAccount.name.trim()

                            val residentPhone = userAccount.phone.trim()

                            Log.d(TAG, "User info: name=$residentName phone=$residentPhone building=$buildingName apt=$apartmentName")

                            // Skip if user didn't provide building/apartment info
                            if (buildingName.isBlank() || apartmentName.isBlank()) {
                                Log.w(TAG, "Building or apartment is blank, skipping apartment registration")
                                return@addOnSuccessListener
                            }

                            val current = _appData.value

                            // Find or create the building
                            var building = current.buildings.find {
                                it.name.trim().equals(buildingName, ignoreCase = true)
                            }
                            var buildings = current.buildings
                            if (building == null) {
                                building = Building(id = IdGenerator.generateId(), name = buildingName)
                                buildings = buildings + building
                                Log.d(TAG, "Created new building: ${building.name}")
                            }
                            val buildingId = building.id

                            val aptId = IdGenerator.generateId()

                            // Find or create the apartment within that building
                            val existingApt = current.apartments.find { apt ->
                                apt.buildingId == buildingId &&
                                apt.name.trim().equals(apartmentName, ignoreCase = true)
                            }

                            val apartments = if (existingApt == null) {
                                val newApt = Apartment(
                                    id = aptId,
                                    buildingId = buildingId,
                                    name = apartmentName,
                                    residentName = residentName,
                                    residentPhone = residentPhone
                                )
                                Log.d(TAG, "Created new apartment: $apartmentName in building: $buildingName with phone: $residentPhone")
                                current.apartments + newApt
                            } else {
                                current.apartments.map { apt ->
                                    if (apt.id == existingApt.id) {
                                        val updatedResName = if (apt.residentName.isBlank()) residentName else apt.residentName
                                        val updatedResPhone = if (apt.residentPhone.isBlank() && residentPhone.isNotBlank()) residentPhone else apt.residentPhone
                                        Log.d(TAG, "Updated existing apartment residentName: $updatedResName phone: $updatedResPhone")
                                        apt.copy(residentName = updatedResName, residentPhone = updatedResPhone)
                                    } else apt
                                }
                            }

                            val updated = current.copy(buildings = buildings, apartments = apartments)
                            saveData(updated)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed to get user doc after approve: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ approveUser FAILED for uid=$uid: ${e.message}", e)
            }
    }

    fun makeAdmin(uid: String) {
        db.collection("users").document(uid)
            .update("role", "admin")
            .addOnSuccessListener { Log.d(TAG, "✅ makeAdmin SUCCESS for uid=$uid") }
            .addOnFailureListener { e -> Log.e(TAG, "❌ makeAdmin FAILED for uid=$uid: ${e.message}", e) }
    }

    fun deleteUser(uid: String) {
        // Note: This only deletes the Firestore doc, not the Auth user.
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "✅ deleteUser SUCCESS for uid=$uid") }
            .addOnFailureListener { e -> Log.e(TAG, "❌ deleteUser FAILED for uid=$uid: ${e.message}", e) }
    }


    fun setContext(context: Context) {
        appContext = context.applicationContext
    }

    fun startListening() {
        if (auth.currentUser == null) return
        if (_userAccount.value?.approved != true) return

        if (listenerRegistration == null) {
            listenerRegistration = mainDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val data = snapshot.toObject(AppData::class.java) ?: AppData()
                        val current = _appData.value
                        _appData.value = data.copy(
                            chat = current.chat,
                            announcements = current.announcements
                        )
                        Log.d(TAG, "Data loaded from Firestore: ${data.expenses.size} expenses")
                    } catch (e: Exception) {
                        Log.e(TAG, "Deserialization failed, applying auto-fix for String IDs: ${e.message}")
                        runFirebaseAutoFixForStringIds()
                    }
                } else {
                    // Create initial document if it doesn't exist
                    Log.d(TAG, "No data found, creating initial doc")
                    mainDocRef.set(AppData())
                }
            }
        }

        startChatListening()
        startAnnouncementListening()
    }

    private fun runFirebaseAutoFixForStringIds() {
        mainDocRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) return@addOnSuccessListener
            val raw = snapshot.data ?: return@addOnSuccessListener
            var changed = false

            fun fixId(map: Map<String, Any>?): Map<String, Any>? {
                if (map == null) return null
                val m = map.toMutableMap()
                val id = m["id"]
                if (id is String) {
                    m["id"] = id.toLongOrNull() ?: 0L
                    changed = true
                }
                val buildingId = m["buildingId"]
                if (buildingId is String) {
                    m["buildingId"] = buildingId.toLongOrNull() ?: 0L
                    changed = true
                }
                return m
            }

            val bList = (raw["buildings"] as? List<Map<String, Any>> ?: emptyList()).mapNotNull { fixId(it) }
            val aList = (raw["apartments"] as? List<Map<String, Any>> ?: emptyList()).mapNotNull { fixId(it) }
            val eList = (raw["expenses"] as? List<Map<String, Any>> ?: emptyList()).mapNotNull { fixId(it) }

            if (changed) {
                val newData = raw.toMutableMap()
                newData["buildings"] = bList
                newData["apartments"] = aList
                newData["expenses"] = eList
                mainDocRef.set(newData).addOnSuccessListener {
                    Log.d(TAG, "Data Auto-Fix successfully applied to Firebase!")
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to apply Data Auto-Fix to Firebase: ${it.message}")
                }
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to read data for Auto-Fix: ${it.message}")
        }
    }


    private fun startChatListening() {
        if (chatListener != null) return
        chatListener = chatCollection.orderBy("id").limitToLast(100).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to chat: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                try {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    val current = _appData.value
                    _appData.value = current.copy(chat = messages)
                } catch (e: Exception) {
                    Log.e(TAG, "Chat deserialization failed, applying fix: ${e.message}")
                    runFirebaseAutoFixForChatMessages(snapshot)
                }
            }
        }
    }

    private fun runFirebaseAutoFixForChatMessages(snapshot: QuerySnapshot) {
        snapshot.documents.forEach { doc ->
            val raw = doc.data ?: return@forEach
            val id = raw["id"]
            if (id is String) {
                val newMap = raw.toMutableMap()
                newMap["id"] = id.toLongOrNull() ?: 0L
                val replies = raw["replies"] as? List<Map<String, Any>>
                if (replies != null) {
                    newMap["replies"] = replies.map { r ->
                        val rId = r["id"]
                        if (rId is String) r.toMutableMap().apply { this["id"] = rId.toLongOrNull() ?: 0L } else r
                    }
                }
                doc.reference.set(newMap)
            }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
        chatListener?.remove()
        chatListener = null
        announcementListener?.remove()
        announcementListener = null
        isInitialLoad = true
    }

    private fun startAnnouncementListening() {
        if (announcementListener != null) return

        // One-time fetch to immediately load announcements (faster than waiting for listener)
        announcementCollection.get()
            .addOnSuccessListener { snapshot ->
                try {
                    val anns = snapshot.toObjects(Announcement::class.java).sortedByDescending { it.id }
                    Log.d(TAG, "Announcements fetched (one-time): ${anns.size}")
                    val current = _appData.value
                    _appData.value = current.copy(announcements = anns)
                } catch (e: Exception) {
                    Log.e(TAG, "Announcement fetch failed, auto-fixing: ${e.message}")
                    runFirebaseAutoFixForAnnouncements(snapshot)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "ANNOUNCEMENT ACCESS DENIED - Check Firestore Rules for 'syndic_announcements': ${e.message}")
            }

        // Real-time listener for updates
        announcementListener = announcementCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to announcements: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    try {
                        val anns = snapshot.toObjects(Announcement::class.java)
                            .sortedByDescending { it.id }

                        if (!isInitialLoad) {
                            val latestAnnouncement = anns.maxByOrNull { it.id }
                            if (latestAnnouncement != null && (lastAnnouncementId == null || latestAnnouncement.id > lastAnnouncementId!!)) {
                                appContext?.let { ctx ->
                                    NotificationHelper.showNewAnnouncementNotification(ctx, latestAnnouncement.title, latestAnnouncement.msg)
                                }
                                lastAnnouncementId = latestAnnouncement.id
                            }
                        } else {
                            isInitialLoad = false
                            lastAnnouncementId = anns.maxByOrNull { it.id }?.id
                        }

                        val current = _appData.value
                        _appData.value = current.copy(announcements = anns)
                        Log.d(TAG, "Announcements loaded: ${anns.size}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Announcement stream failed, auto-fixing: ${e.message}")
                        runFirebaseAutoFixForAnnouncements(snapshot)
                    }
                }
            }
    }

    private fun runFirebaseAutoFixForAnnouncements(snapshot: QuerySnapshot) {
        snapshot.documents.forEach { doc ->
            val raw = doc.data ?: return@forEach
            val id = raw["id"]
            if (id is String) {
                val newMap = raw.toMutableMap()
                newMap["id"] = id.toLongOrNull() ?: 0L
                val comments = raw["comments"] as? List<Map<String, Any>>
                if (comments != null) {
                    newMap["comments"] = comments.map { c ->
                        val cId = c["id"]
                        if (cId is String) c.toMutableMap().apply { this["id"] = cId.toLongOrNull() ?: 0L } else c
                    }
                }
                doc.reference.set(newMap)
            }
        }
    }



    fun saveData(data: AppData) {
        if (auth.currentUser == null) return
        val toSave = data.copy(
            chat = emptyList(),
            announcements = emptyList()
        )
        mainDocRef.set(toSave)
            .addOnSuccessListener { Log.d(TAG, "Main data saved successfully") }
            .addOnFailureListener { e -> Log.e(TAG, "Error saving main data", e) }
    }


    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        phone: String = "",
        apartment: String,
        building: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    val newAccount = UserAccount(
                        uid = uid, 
                        email = email, 
                        role = "resident", 
                        approved = false,
                        name = name,
                        phone = phone,
                        apartment = apartment,
                        building = building
                    )
                    db.collection("users").document(uid).set(newAccount)
                        .addOnCompleteListener { onResult(true, null) }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _appData.value = AppData()
        _userAccount.value = null
        isInitialLoad = true
        lastAnnouncementId = null
        stopListening()
        stopUserListening()
    }

    fun changePassword(newPwd: String, onResult: (Boolean, String?) -> Unit) {
        auth.currentUser?.updatePassword(newPwd)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Buildings
    fun addBuilding(name: String) {
        val current = _appData.value
        val newBuilding = Building(
            id = IdGenerator.generateId(),
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
            id = IdGenerator.generateId(),
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
        savePaymentWithReceipt(apartmentId, year, month, amount, base64Image = null, removeReceipt = false)
    }

    fun savePaymentWithReceipt(
        apartmentId: Long,
        year: Int,
        month: Int,
        amount: Double,
        base64Image: String? = null,
        removeReceipt: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        val current = _appData.value
        val key = "${apartmentId}_${year}_${month}"
        val updatedPayments = current.payments.toMutableMap()
        updatedPayments[key] = amount

        val updatedApartments = current.apartments.map {
            if (it.id == apartmentId) it.copy(lastPaidTime = System.currentTimeMillis(), lastTotal = amount) else it
        }

        val updatedReceipts = current.paymentReceipts.toMutableMap()
        if (base64Image != null) {
            updatedReceipts[key] = true
            paymentReceiptsCollection.document(key)
                .set(mapOf("image" to base64Image))
                .addOnSuccessListener {
                    Log.d(TAG, "Payment receipt image saved for $key")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving payment receipt for $key", e)
                    onComplete()
                }
        } else if (removeReceipt) {
            updatedReceipts.remove(key)
            paymentReceiptsCollection.document(key).delete()
            onComplete()
        } else {
            onComplete()
        }

        val updated = current.copy(
            payments = updatedPayments,
            apartments = updatedApartments,
            paymentReceipts = updatedReceipts
        )
        saveData(updated)
    }

    fun clearPayment(apartmentId: Long, year: Int, month: Int) {
        val current = _appData.value
        val key = "${apartmentId}_${year}_${month}"
        val updatedPayments = current.payments.toMutableMap()
        updatedPayments.remove(key)

        val updatedReceipts = current.paymentReceipts.toMutableMap()
        updatedReceipts.remove(key)
        paymentReceiptsCollection.document(key).delete()

        val updated = current.copy(
            payments = updatedPayments,
            paymentReceipts = updatedReceipts
        )
        saveData(updated)
    }

    fun getPaymentAmount(apartmentId: Long, year: Int, month: Int): Double {
        val key = "${apartmentId}_${year}_${month}"
        return _appData.value.payments[key] ?: 0.0
    }

    fun getPaymentReceiptImage(apartmentId: Long, year: Int, month: Int, onResult: (String?) -> Unit) {
        val key = "${apartmentId}_${year}_${month}"
        paymentReceiptsCollection.document(key).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.getString("image"))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // Expenses
    fun addExpense(expense: Expense) {
        val current = _appData.value
        val updated = current.copy(expenses = current.expenses + expense)
        saveData(updated)
        // Fire notification to inform all users about the new expense
        appContext?.let { ctx ->
            NotificationHelper.showNewExpenseNotification(ctx, expense.desc, expense.amount)
        }
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

        // Fire notification to inform all users about the new expense
        appContext?.let { ctx ->
            NotificationHelper.showNewExpenseNotification(ctx, expense.desc, expense.amount)
        }

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
        announcementCollection.document(announcement.id.toString()).set(announcement)
    }


    fun addAnnouncementWithImage(announcement: Announcement, base64Image: String?, onComplete: () -> Unit = {}) {
        announcementCollection.document(announcement.id.toString()).set(announcement)
            .addOnSuccessListener {
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
            .addOnFailureListener {
                Log.e(TAG, "Error adding announcement", it)
                onComplete()
            }
    }


    fun deleteAnnouncement(announcementId: Long) {
        // Delete image if exists
        announcementsImagesCollection.document(announcementId.toString()).delete()
        // Delete announcement document
        announcementCollection.document(announcementId.toString()).delete()
    }


    fun addComment(announcementId: Long, comment: Comment) {
        val annRef = announcementCollection.document(announcementId.toString())
        db.runTransaction { transaction ->
            val snapshot = transaction.get(annRef)
            val currentAnn = snapshot.toObject(Announcement::class.java)
            if (currentAnn != null) {
                val updatedComments = currentAnn.comments + comment
                transaction.update(annRef, "comments", updatedComments)
            }
        }.addOnFailureListener { Log.e(TAG, "Error adding comment", it) }
    }

    fun deleteComment(announcementId: Long, commentId: Long) {
        val annRef = announcementCollection.document(announcementId.toString())
        db.runTransaction { transaction ->
            val snapshot = transaction.get(annRef)
            val currentAnn = snapshot.toObject(Announcement::class.java)
            if (currentAnn != null) {
                val filtered = currentAnn.comments.filter { it.id != commentId }
                transaction.update(annRef, "comments", filtered)
            }
        }.addOnFailureListener { Log.e(TAG, "Error deleting comment", it) }
    }


    fun saveAnnouncementImage(announcementId: Long, base64Image: String, onComplete: () -> Unit) {
        announcementsImagesCollection.document(announcementId.toString())
            .set(mapOf("image" to base64Image))
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }

    // Chat
    fun sendChatMessage(message: ChatMessage) {
        val currentUser = auth.currentUser ?: return
        val enrichedMessage = message.copy(
            senderUid = currentUser.uid
        )
        chatCollection.document(enrichedMessage.id.toString()).set(enrichedMessage)
            .addOnSuccessListener { Log.d(TAG, "Chat message sent successfully: ${enrichedMessage.id}") }
            .addOnFailureListener { e -> Log.e(TAG, "Error sending chat message: ${e.message}", e) }
    }


    fun deleteChatMessage(messageId: Long) {
        chatCollection.document(messageId.toString()).delete()
    }

    fun likeChatMessage(messageId: Long) {
        val msgRef = chatCollection.document(messageId.toString())
        db.runTransaction { transaction ->
            val snapshot = transaction.get(msgRef)
            val currentLikes = snapshot.getLong("likes") ?: 0L
            transaction.update(msgRef, "likes", currentLikes + 1)
        }.addOnFailureListener { Log.e(TAG, "Error liking message", it) }
    }

    fun dislikeChatMessage(messageId: Long) {
        val msgRef = chatCollection.document(messageId.toString())
        db.runTransaction { transaction ->
            val snapshot = transaction.get(msgRef)
            val currentDislikes = snapshot.getLong("dislikes") ?: 0L
            transaction.update(msgRef, "dislikes", currentDislikes + 1)
        }.addOnFailureListener { Log.e(TAG, "Error disliking message", it) }
    }

    fun addChatReply(messageId: Long, reply: Comment) {
        val msgRef = chatCollection.document(messageId.toString())
        db.runTransaction { transaction ->
            val snapshot = transaction.get(msgRef)
            val currentReplies = snapshot.toObject(ChatMessage::class.java)?.replies ?: emptyList()
            transaction.update(msgRef, "replies", currentReplies + reply)
        }.addOnFailureListener { Log.e(TAG, "Error adding chat reply", it) }
    }

    fun deleteChatReply(messageId: Long, replyId: Long) {
        val msgRef = chatCollection.document(messageId.toString())
        db.runTransaction { transaction ->
            val snapshot = transaction.get(msgRef)
            val currentReplies = snapshot.toObject(ChatMessage::class.java)?.replies ?: emptyList()
            val filtered = currentReplies.filter { it.id != replyId }
            transaction.update(msgRef, "replies", filtered)
        }.addOnFailureListener { Log.e(TAG, "Error deleting chat reply", it) }
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

        return Triple(filteredIn, filteredOut, filteredIn - filteredOut)
    }
}

