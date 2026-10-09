package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Send

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.syndic.zaineb4.R
import com.example.syndic.zaineb4.data.Announcement
import com.example.syndic.zaineb4.data.ChatMessage
import com.example.syndic.zaineb4.data.Comment
import com.example.syndic.zaineb4.data.UserAccount
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(viewModel: SyndicViewModel) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val isAdmin = userAccount?.role == "admin"

    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var selectedAnnouncementId by remember { mutableLongStateOf(0L) }

    // Image state for new announcement
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Deletion states
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var deleteItemType by remember { mutableStateOf("") } // "announcement", "chat", "comment", "reply"
    var deleteItemId by remember { mutableLongStateOf(0L) }
    var deleteParentId by remember { mutableLongStateOf(0L) } // For comments/replies
    var showLogoutDialog by remember { mutableStateOf(false) }

    // We no longer need chatName as a mutable state because it's auto-generated from userAccount


    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val resized = resizeBitmap(bitmap)
            selectedBitmap = resized
            base64Image = bitmapToBase64(resized)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val resized = resizeBitmap(bitmap)
                selectedBitmap = resized
                base64Image = bitmapToBase64(resized)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_communication)) },
                actions = {
                    if (isAuthenticated) {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.btn_locked), tint = Color.Green)
                        }
                    }

                    if (showLogoutDialog) {
                        LogoutConfirmationDialog(
                            onConfirm = {
                                showLogoutDialog = false
                                viewModel.logout()
                            },
                            onDismiss = { showLogoutDialog = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Announcements Section
            Text(
                stringResource(R.string.title_announcements),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Add Announcement
            if (isAdmin) {
                var title by remember { mutableStateOf("") }
                var message by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.ph_announce_title)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text(stringResource(R.string.ph_announce_msg)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Image Selection and Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showImageSourceDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (selectedBitmap == null) "📸 إضافة صورة" else "📸 تغيير الصورة")
                            }

                            if (selectedBitmap != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box {
                                    Image(
                                        bitmap = selectedBitmap!!.asImageBitmap(),
                                        contentDescription = "Preview",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clickable { selectedBitmap = null; base64Image = null },
                                        contentScale = ContentScale.Crop
                                    )
                                    Text("❌", modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(12.dp), fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                            if (isAdmin) {
                                if (title.isNotBlank() && message.isNotBlank()) {
                                    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                    val annId = System.currentTimeMillis()
                                    val announcement = Announcement(
                                        id = annId,
                                        title = title,
                                        msg = message,
                                        date = dateStr,
                                        hasImage = base64Image != null,
                                        comments = emptyList()
                                    )
                                    viewModel.addAnnouncementWithImage(announcement, base64Image)
                                    title = ""
                                    message = ""
                                    selectedBitmap = null
                                    base64Image = null
                                }
                            }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.btn_add_announce))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Announcements List
            if (appData.announcements.isEmpty()) {
                Text(
                    "No announcements yet.",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                appData.announcements.forEach { announcement ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .heightIn(min = 80.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        onClick = {
                            selectedAnnouncementId = announcement.id
                            showAnnouncementDialog = true
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    announcement.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (announcement.hasImage) {
                                    Text("🖼️")
                                }
                            }

                            Text(
                                "🕒 ${announcement.date}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                announcement.msg,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                stringResource(R.string.btn_details),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chat Section
            Text(
                stringResource(R.string.title_chat),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chat Messages Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp), // Increased height for better view
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (appData.chat.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Start chatting now...", color = Color.Gray)
                    }
                } else {
                    val currentUid = userAccount?.uid ?: ""
                    val visibleMessages = appData.chat.filter { msg ->
                        // Private messages are strictly for admin ONLY
                        !msg.isPrivate || isAdmin
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = false, // Keep normal order but we can adjust if needed
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visibleMessages) { msg ->
                            val isMe = msg.senderUid == currentUid
                            ChatMessageItem(
                                message = msg,
                                isMe = isMe,
                                isAdmin = isAdmin,
                                onDelete = {
                                    if (isAdmin) {
                                        deleteItemId = msg.id
                                        deleteItemType = "chat"
                                        showDeleteConfirmation = true
                                    }
                                },
                                onLike = { viewModel.likeChatMessage(msg.id) },
                                onDislike = { viewModel.dislikeChatMessage(msg.id) },
                                onAddReply = { text ->
                                    val displayName = when {
                                    userAccount?.role == "admin" -> "Admin"
                                    else -> userAccount?.name ?: "Resident"
                                }
                                    val dateStr = java.text.SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
                                    val reply = Comment(
                                        id = System.currentTimeMillis(),
                                        name = displayName,
                                        text = text,
                                        date = dateStr
                                    )
                                    viewModel.addChatReply(msg.id, reply)
                                },
                                onDeleteReply = { replyId ->
                                    deleteItemId = replyId
                                    deleteParentId = msg.id
                                    deleteItemType = "reply"
                                    showDeleteConfirmation = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Send Message Controls
            var chatMessage by remember { mutableStateOf("") }
            var isPrivateMessage by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPrivateMessage,
                            onCheckedChange = { isPrivateMessage = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color.Red)
                        )
                        Text(stringResource(R.string.lbl_private_to_admin), fontSize = 13.sp, color = if (isPrivateMessage) Color.Red else Color.Unspecified)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display Auto-filled Name/Apt Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(bottom = 4.dp).height(56.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val displayName = when {
                                    userAccount?.role == "admin" -> "Admin"
                                    else -> userAccount?.name ?: "Resident"
                                }
                                Text(
                                    text = displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.ph_chat_name),
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }


                        OutlinedTextField(
                            value = chatMessage,
                            onValueChange = { chatMessage = it },
                            label = { Text(stringResource(R.string.ph_chat_msg)) },
                            modifier = Modifier.weight(1f),
                            maxLines = 3
                        )

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (chatMessage.isNotBlank()) {
                                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                val displayName = when {
                                    userAccount?.role == "admin" -> "Admin"
                                    else -> userAccount?.name ?: "Resident"
                                }
                                val message = ChatMessage(
                                    id = System.currentTimeMillis(),
                                    name = displayName,
                                    msg = chatMessage,
                                    time = timeStr,
                                    isPrivate = isPrivateMessage,
                                    senderUid = userAccount?.uid ?: ""
                                )
                                viewModel.sendChatMessage(message)
                                chatMessage = ""
                                isPrivateMessage = false
                            }

                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))


                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_send_chat))
                    }
                }
            }
        }
    }


    if (showAnnouncementDialog) {
        val announcement = appData.announcements.find { it.id == selectedAnnouncementId }
        announcement?.let {
            AnnouncementDialog(
                announcement = it,
                isAdmin = isAdmin,
                userAccount = userAccount,
                onAddComment = { _, text -> 
                    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    val displayName = when {
                        userAccount?.role == "admin" -> "Admin"
                        else -> userAccount?.name ?: "Resident"
                    }
                    val comment = Comment(
                        id = System.currentTimeMillis(),
                        name = displayName,
                        text = text,
                        date = dateStr
                    )
                    viewModel.addComment(it.id, comment)
                },

                onDeleteComment = { commentId ->
                    deleteItemId = commentId
                    deleteParentId = it.id
                    deleteItemType = "comment"
                    showDeleteConfirmation = true
                },
                onDeleteAnnouncement = {
                    deleteItemId = it.id
                    deleteItemType = "announcement"
                    showDeleteConfirmation = true
                    showAnnouncementDialog = false
                },
                onDismiss = { showAnnouncementDialog = false }
            )
        }
    }

    if (showDeleteConfirmation) {
        val title = when (deleteItemType) {
            "announcement" -> "حذف الإعلان"
            "chat" -> "حذف الرسالة"
            "comment", "reply" -> "حذف التعليق"
            else -> "حذف"
        }
        val message = when (deleteItemType) {
            "announcement" -> "هل أنت متأكد من حذف هذا الإعلان نهائياً؟"
            "chat" -> "هل أنت متأكد من حذف هذه الرسالة؟"
            "comment", "reply" -> "هل أنت متأكد من حذف هذا التعليق؟"
            else -> "هل أنت متأكد من الحذف؟"
        }

        DeleteDialog(
            title = title,
            message = message,
            onConfirm = {
                when (deleteItemType) {
                    "announcement" -> viewModel.deleteAnnouncement(deleteItemId)
                    "chat" -> viewModel.deleteChatMessage(deleteItemId)
                    "comment" -> viewModel.deleteComment(deleteParentId, deleteItemId)
                    "reply" -> viewModel.deleteChatReply(deleteParentId, deleteItemId)
                }
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
    if (showImageSourceDialog) {
        Dialog(onDismissRequest = { showImageSourceDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("إختيار مصدر الصورة", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            cameraLauncher.launch()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷 كاميرا")
                    }

                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖼️ " + stringResource(R.string.btn_gallery))
                    }

                    TextButton(onClick = { showImageSourceDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isMe: Boolean,
    isAdmin: Boolean,
    onDelete: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onAddReply: (String) -> Unit,
    onDeleteReply: (Long) -> Unit
) {
    var showReplyDialog by remember { mutableStateOf(false) }

    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9)
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
            if (message.isPrivate) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(10.dp), tint = Color.Red)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.badge_private), fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
            }
            val displayedName = message.name.ifBlank { "Resident" }
            Text(
                displayedName,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Surface(
            shape = shape,
            color = bubbleColor,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                val displayMsg = if (message.isPrivate && !isAdmin && !isMe) {
                    "•••••• (" + stringResource(R.string.badge_private) + ")"
                } else {
                    message.msg
                }
                Text(
                    displayMsg,
                    fontSize = 14.sp,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                )
                
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        message.time,
                        fontSize = 9.sp,
                        color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else Color.Gray
                    )
                    
                    if (isAdmin) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        // Interaction Mini-Bar
        Row(
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLike() }) {
                Icon(Icons.Default.ThumbUp, null, modifier = Modifier.size(14.dp), tint = if (message.likes > 0) MaterialTheme.colorScheme.primary else Color.Gray)
                if (message.likes > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text("${message.likes}", fontSize = 11.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onDislike() }) {
                Icon(Icons.Default.ThumbDown, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                if (message.dislikes > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text("${message.dislikes}", fontSize = 11.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showReplyDialog = true }) {
                Icon(Icons.Default.Comment, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.btn_reply), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }

        // Replies List
        if (message.replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(start = if (isMe) 0.dp else 24.dp, end = if (isMe) 24.dp else 0.dp, top = 4.dp)
                    .widthIn(max = 260.dp),
                horizontalAlignment = alignment
            ) {
                message.replies.forEach { reply ->
                    Surface(
                        modifier = Modifier.padding(vertical = 1.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    reply.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                if (isAdmin) {
                                    IconButton(onClick = { onDeleteReply(reply.id) }, modifier = Modifier.size(14.dp)) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                            Text(reply.text, fontSize = 12.sp)
                            Text(reply.date, fontSize = 8.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
    }

    if (showReplyDialog) {
        var replyText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showReplyDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.title_add_reply), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text(stringResource(R.string.btn_reply) + "...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showReplyDialog = false }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (replyText.isNotBlank()) {
                                onAddReply(replyText)
                                showReplyDialog = false
                            }
                        }) {
                            Text(stringResource(R.string.btn_send))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AnnouncementDialog(
    announcement: Announcement,
    isAdmin: Boolean,
    userAccount: UserAccount?,
    onAddComment: (String, String) -> Unit,
    onDeleteComment: (Long) -> Unit,
    onDeleteAnnouncement: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Text(
                    announcement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date
                Text(
                    "🕒 ${announcement.date}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    announcement.msg,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )

                // Image Section
                if (announcement.hasImage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
                    var isLoading by remember { mutableStateOf(true) }
                    
                    LaunchedEffect(announcement.id) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("syndic_announcements_images").document(announcement.id.toString()).get()
                            .addOnSuccessListener { snapshot ->
                                val base64 = snapshot.getString("image")
                                if (base64 != null) {
                                    try {
                                        val decodedString = Base64.decode(base64, Base64.DEFAULT)
                                        imageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                isLoading = false
                            }
                            .addOnFailureListener {
                                isLoading = false
                            }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                            } else if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap!!.asImageBitmap(),
                                    contentDescription = "Announcement Image",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text("❌ خطأ في تحميل الصورة", modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }

                // Delete button for admin
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDeleteAnnouncement,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_del_ann))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comments Section
                Text(
                    stringResource(R.string.title_comments),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (announcement.comments.isEmpty()) {
                    Text(
                        stringResource(R.string.no_comments),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                } else {
                    announcement.comments.forEach { comment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        comment.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        comment.date,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    comment.text,
                                    fontSize = 14.sp
                                )

                                if (isAdmin) {
                                    TextButton(
                                        onClick = { onDeleteComment(comment.id) }
                                    ) {
                                        Text("Delete", color = Color.Red, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display Auto-filled Identity for Comment
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayName = when {
                            userAccount?.role == "admin" -> "Admin"
                            else -> userAccount?.name ?: "Resident"
                        }
                        Text(
                            text = "${stringResource(R.string.ph_chat_name)}: ",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                var commentText by remember { mutableStateOf("") }


                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text(stringResource(R.string.ph_comment)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment("", commentText) // Name is now auto-filled in the callback
                            commentText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_add_comment))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}

private fun resizeBitmap(bitmap: Bitmap): Bitmap {
    val maxWidth = 1200
    val maxHeight = 1200
    var width = bitmap.width
    var height = bitmap.height

    if (width > maxWidth || height > maxHeight) {
        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxWidth
            height = (maxWidth / ratio).toInt()
        } else {
            height = maxHeight
            width = (maxHeight * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    return bitmap
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}


