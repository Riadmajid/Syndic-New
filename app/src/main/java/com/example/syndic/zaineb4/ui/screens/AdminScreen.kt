package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.syndic.zaineb4.data.UserAccount
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: SyndicViewModel,
    onNavigateBack: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val pendingUsers = allUsers.filter { !it.approved }
    val approvedUsers = allUsers.filter { it.approved }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserAccount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المستخدمين", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E3C72),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pendingUsers.isNotEmpty()) {
                item {
                    SectionHeader("طلبات الانضمام المعلقة", Color(0xFFE67E22))
                }
                items(pendingUsers) { user ->
                    UserCard(user, viewModel, isPending = true, onDeleteClick = {
                        userToDelete = it
                        showDeleteDialog = true
                    })
                }
            }

            if (approvedUsers.isNotEmpty()) {
                item {
                    SectionHeader("المستخدمون المعتمدون", Color(0xFF27AE60))
                }
                items(approvedUsers) { user ->
                    UserCard(user, viewModel, isPending = false, onDeleteClick = {
                        userToDelete = it
                        showDeleteDialog = true
                    })
                }
            }
        }

        if (showDeleteDialog && userToDelete != null) {
            val isPending = !userToDelete!!.approved
            DeleteDialog(
                title = if (isPending) "رفض الطلب" else "حذف المستخدم",
                message = if (isPending) 
                    "هل أنت متأكد من رفض طلب انضمام ${userToDelete!!.email}؟" 
                    else "هل أنت متأكد من حذف حساب ${userToDelete!!.email} نهائياً؟",
                onConfirm = {
                    viewModel.deleteUser(userToDelete!!.uid)
                    showDeleteDialog = false
                    userToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    userToDelete = null
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 24.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun UserCard(
    user: UserAccount,
    viewModel: SyndicViewModel,
    isPending: Boolean,
    onDeleteClick: (UserAccount) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = user.email,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (user.role != "admin") {
                        Text(
                            text = "${user.name} - ${user.building} - ${user.apartment}",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        if (user.phone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📞 ${user.phone}",
                                    color = Color(0xFF1E3C72),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Call",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val cleanPhone = user.phone.replace(Regex("[^0-9+]"), "")
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = if (user.role == "admin") "مسؤول (Admin)" else "ساكن (Resident)",
                        color = if (user.role == "admin") Color(0xFF1E3C72) else Color.Gray,
                        fontSize = 12.sp
                    )
                }

                if (isPending) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.approveUser(user.uid) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("موافقة", fontSize = 14.sp)
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                onDeleteClick(user)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Red)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إلغاء", fontSize = 14.sp)
                        }
                    }
                } else {
                    Row {
                        if (user.role != "admin") {
                            IconButton(onClick = { viewModel.makeAdmin(user.uid) }) {
                                Icon(Icons.Default.Security, contentDescription = "Make Admin", tint = Color(0xFF1E3C72))
                            }
                        }
                        IconButton(onClick = { 
                            onDeleteClick(user)
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
