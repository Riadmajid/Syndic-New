package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel

@Composable
fun PendingApprovalScreen(viewModel: SyndicViewModel) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color(0xFFE67E22)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "الحساب قيد المراجعة",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "شكراً لتسجيلك. حسابك في حالة انتظار موافقة المسؤول (السانديك). يرجى العودة لاحقاً أو التواصل مع الإدارة.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34495E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج", fontSize = 16.sp)
            }
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
}
