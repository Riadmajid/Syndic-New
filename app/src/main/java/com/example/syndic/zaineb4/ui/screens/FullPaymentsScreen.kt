package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.FileProvider
import android.widget.Toast
import com.example.syndic.zaineb4.utils.PdfHelper
import com.example.syndic.zaineb4.utils.ReceiptHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syndic.zaineb4.R
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPaymentsScreen(
    viewModel: SyndicViewModel,
    onNavigateBack: () -> Unit
) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExcelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedBuildingId by remember { mutableLongStateOf(0L) }

    val years = (2014..2040).toList()
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectedBuildingId != 0L) {
                        val bld = appData.buildings.find { it.id == selectedBuildingId }
                        Text("🏢 ${bld?.name ?: ""}")
                    } else {
                        Text(stringResource(R.string.full_table_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedBuildingId != 0L) {
                            selectedBuildingId = 0L
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back_dash))
                    }
                },
                actions = {
                    // Language selection
                    var langExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { langExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
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
                                    com.example.syndic.zaineb4.utils.LocaleHelper.setLocale("ar")
                                    langExpanded = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Français") },
                                onClick = {
                                    com.example.syndic.zaineb4.utils.LocaleHelper.setLocale("fr")
                                    langExpanded = false
                                }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    com.example.syndic.zaineb4.utils.LocaleHelper.setLocale("en")
                                    langExpanded = false
                                }
                            )
                        }
                    }

                    if (isAuthenticated) {
                        if (userAccount?.role == "admin") {
                            IconButton(onClick = { showExcelDialog = true }) {
                                Text("📄", fontSize = 18.sp)
                            }
                        }
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

                    if (showExcelDialog) {
                        PdfExportDialog(
                            initialYear = selectedYear,
                            initialMonth = null,
                            onDismiss = { showExcelDialog = false },
                            onConfirm = { expYear, expMonth ->
                                showExcelDialog = false
                                val file = PdfHelper.exportBilanToPdf(appData, expYear, expMonth, context)
                                if (file != null) {
                                    PdfHelper.handlePdfExport(context, file)
                                } else {
                                    Toast.makeText(context, "Error creating PDF file", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
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
            if (selectedBuildingId == 0L) {
                // Building selection
                Text(
                    stringResource(R.string.title_select_bld),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (appData.buildings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_apts),
                            color = Color.Gray
                        )
                    }
                } else {
                    appData.buildings.forEach { building ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedBuildingId = building.id },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "🏢 ${building.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Full Payment Table for selected building
                val buildingApts = appData.apartments.filter { it.buildingId == selectedBuildingId }

                // Year selector
                var yearExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    OutlinedTextField(
                        value = "${stringResource(R.string.year_word)} $selectedYear",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .width(140.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text("${stringResource(R.string.year_word)} $year") },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (buildingApts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_apts),
                            color = Color.Gray
                        )
                    }
                } else {
                    // Scrollable table
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column {
                            // Header row
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.apt_month),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(120.dp),
                                    textAlign = TextAlign.Center
                                )
                                months.forEach { month ->
                                    Text(
                                        month,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Data rows
                            val currentBld = appData.buildings.find { it.id == selectedBuildingId }
                            val isAdmin = userAccount?.role == "admin"
                            buildingApts.forEach { apt ->
                                val isMyApt = !isAdmin && userAccount != null && isApartmentOfResident(apt, currentBld?.name, userAccount)
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .background(if (isMyApt) Color(0xFFF0FDF4) else Color.White)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.width(120.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            apt.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Start
                                        )
                                        if (isMyApt) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("🏠", fontSize = 11.sp)
                                        }
                                    }
                                    for (month in 1..12) {
                                        val amount = appData.payments["${apt.id}_${selectedYear}_${month}"]
                                        val isPaid = amount != null && amount > 0
                                        Box(
                                            modifier = Modifier
                                                .width(60.dp)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isPaid) Color(0xFF10B981) else Color(0xFFFEE2E2)
                                                )
                                                .then(
                                                    if (isMyApt && isPaid) Modifier.clickable {
                                                        ReceiptHelper.downloadPaymentReceipt(
                                                            context = context,
                                                            apartmentId = apt.id,
                                                            year = selectedYear,
                                                            month = month,
                                                            apartmentName = apt.name,
                                                            buildingName = currentBld?.name ?: "",
                                                            residentName = apt.residentName.ifBlank { userAccount?.name ?: "" },
                                                            amount = amount ?: 0.0
                                                        )
                                                    } else Modifier
                                                )
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                if (isPaid) {
                                                    if (isMyApt) "✓ 📥" else "✓"
                                                } else "✗",
                                                color = if (isPaid) Color.White else Color(0xFF991B1B),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

