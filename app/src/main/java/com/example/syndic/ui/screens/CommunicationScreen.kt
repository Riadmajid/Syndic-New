package com.example.syndic.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.syndic.R
import com.example.syndic.data.Announcement
import com.example.syndic.data.ChatMessage
import com.example.syndic.data.Comment
import com.example.syndic.viewmodel.SyndicViewModel
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

    var showAuthDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var selectedAnnouncementId by remember { mutableLongStateOf(0L) }

    // Image state for new announcement
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

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
                    // Language selection
                    var langExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { langExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Change Language"
                            )
                        }
                        DropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("العربية") },
                                onClick = {
                                    com.example.syndic.utils.LocaleHelper.setLocale("ar")
                                    langExpanded = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Français") },
                                onClick = {
                                    com.example.syndic.utils.LocaleHelper.setLocale("fr")
                                    langExpanded = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    com.example.syndic.utils.LocaleHelper.setLocale("en")
                                    langExpanded = false
                                }
                            )
                        }
                    }

                    if (isAuthenticated) {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.btn_locked), tint = Color.Green)
                        }
                    } else {
                        IconButton(onClick = { showAuthDialog = true }) {
                            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.btn_unlocked))
                        }
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
            if (isAuthenticated) {
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

            // Chat Messages
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    if (appData.chat.isEmpty()) {
                        Text(
                            "Start chatting now...",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column {
                            appData.chat.forEach { msg ->
                                ChatMessageItem(
                                    message = msg,
                                    isAuthenticated = isAuthenticated,
                                    onDelete = {
                                        if (isAuthenticated) {
                                            viewModel.deleteChatMessage(msg.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Send Message
            var chatName by remember { mutableStateOf("") }
            var chatMessage by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatName,
                    onValueChange = { chatName = it },
                    label = { Text(stringResource(R.string.ph_chat_name)) },
                    modifier = Modifier.weight(0.4f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = chatMessage,
                    onValueChange = { chatMessage = it },
                    label = { Text(stringResource(R.string.ph_chat_msg)) },
                    modifier = Modifier.weight(0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (chatMessage.isNotBlank()) {
                        val timeStr = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        val name = chatName.ifBlank { "Resident" }
                        val message = ChatMessage(
                            id = System.currentTimeMillis(),
                            name = name,
                            msg = chatMessage,
                            time = timeStr
                        )
                        viewModel.sendChatMessage(message)
                        chatMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_send_chat))
            }
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            onConfirm = { pwd ->
                if (viewModel.verifyPassword(pwd)) {
                    showAuthDialog = false
                }
            },
            onDismiss = { showAuthDialog = false }
        )
    }

    if (showAnnouncementDialog) {
        val announcement = appData.announcements.find { it.id == selectedAnnouncementId }
        announcement?.let {
            AnnouncementDialog(
                announcement = it,
                isAuthenticated = isAuthenticated,
                onAddComment = { name, text ->
                    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    val comment = Comment(
                        id = System.currentTimeMillis(),
                        name = name.ifBlank { "Resident" },
                        text = text,
                        date = dateStr
                    )
                    viewModel.addComment(it.id, comment)
                },
                onDeleteComment = { commentId ->
                    viewModel.deleteComment(it.id, commentId)
                },
                onDeleteAnnouncement = {
                    viewModel.deleteAnnouncement(it.id)
                    showAnnouncementDialog = false
                },
                onDismiss = { showAnnouncementDialog = false }
            )
        }
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
                        Text("🖼️ معرض الصور")
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
    isAuthenticated: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    message.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    message.msg,
                    fontSize = 14.sp
                )
                Text(
                    message.time,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            if (isAuthenticated) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementDialog(
    announcement: Announcement,
    isAuthenticated: Boolean,
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
                if (isAuthenticated) {
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

                                if (isAuthenticated) {
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

                // Add Comment
                var commentName by remember { mutableStateOf("") }
                var commentText by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = commentName,
                    onValueChange = { commentName = it },
                    label = { Text(stringResource(R.string.ph_chat_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

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
                            onAddComment(commentName, commentText)
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

