package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syndic.zaineb4.R
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel
import com.example.syndic.zaineb4.utils.PdfHelper
import com.example.syndic.zaineb4.utils.ReceiptHelper
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.FileProvider
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SyndicViewModel,
    onNavigateToFullPayments: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExcelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var monthFilter by remember { mutableStateOf("all") }

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val years = (2014..2040).toList()

    // Calculate stats
    val stats = remember(appData, selectedMonth, selectedYear, monthFilter) {
        val month = if (monthFilter == "all") null else selectedMonth
        viewModel.calculateStats(month, selectedYear)
    }

    val (incomes, expenses, balance) = stats

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_dashboard)) },
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
                        androidx.compose.material3.DropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("العربية") },
                                onClick = {
                                    com.example.syndic.zaineb4.utils.LocaleHelper.setLocale("ar")
                                    langExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Français") },
                                onClick = {
                                    com.example.syndic.zaineb4.utils.LocaleHelper.setLocale("fr")
                                    langExpanded = false
                                }
                            )
                            DropdownMenuItem(
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
                            IconButton(onClick = onNavigateToAdmin) {
                                Icon(Icons.Default.People, contentDescription = "Manage Users")
                            }
                            IconButton(onClick = { showExcelDialog = true }) {
                                Text("📄", fontSize = 18.sp)
                            }
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.btn_locked),
                                tint = Color.Green
                            )
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
                            initialMonth = if (monthFilter == "all") null else selectedMonth,
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
            // Resident Quick Card
            if (userAccount?.role != "admin" && userAccount != null) {
                val myApt = remember(appData.apartments, appData.buildings, userAccount) {
                    findResidentApartment(appData.apartments, appData.buildings, userAccount)
                }
                val myBld = if (myApt != null) appData.buildings.find { it.id == myApt.buildingId } else null
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🏠 شقتي: ${myApt?.name ?: userAccount!!.apartment}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "🏢 ${myBld?.name ?: userAccount!!.building}",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }
                            if (myApt != null) {
                                val currentMonthAmount = appData.payments["${myApt.id}_${selectedYear}_${currentMonth}"] ?: 0.0
                                if (currentMonthAmount > 0) {
                                    androidx.compose.material3.FilledTonalButton(
                                        onClick = {
                                            ReceiptHelper.downloadPaymentReceipt(
                                                context = context,
                                                apartmentId = myApt.id,
                                                year = selectedYear,
                                                month = currentMonth,
                                                apartmentName = myApt.name,
                                                buildingName = myBld?.name ?: "",
                                                residentName = myApt.residentName.ifBlank { userAccount?.name ?: "" },
                                                amount = currentMonthAmount
                                            )
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFF10B981),
                                            contentColor = Color.White
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("📥 وصل ${months[currentMonth - 1]}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stats Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.title_stats_filter),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                

                Row {
                    // Month Filter
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (monthFilter == "all") stringResource(R.string.opt_all_months) else months[selectedMonth - 1],
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.height(20.dp)) },
                            modifier = Modifier
                                .menuAnchor()
                                .width(105.dp)
                                .height(50.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.opt_all_months)) },
                                onClick = {
                                    monthFilter = "all"
                                    expanded = false
                                }
                            )
                            months.forEachIndexed { index, month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        selectedMonth = index + 1
                                        monthFilter = "month"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Year Filter
                    var yearExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "$selectedYear",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.height(20.dp)) },
                            modifier = Modifier
                                .menuAnchor()
                                .width(85.dp)
                                .height(50.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            shape = RoundedCornerShape(8.dp)
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Incomes Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💰", fontSize = 32.sp)
                        Text(stringResource(R.string.stat_incomes_month), color = Color.White, fontSize = 12.sp)
                        Text(
                            "${incomes.toInt()} ${stringResource(R.string.curr)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Expenses Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📉", fontSize = 32.sp)
                        Text(stringResource(R.string.stat_expenses_month), color = Color.White, fontSize = 12.sp)
                        Text(
                            "${expenses.toInt()} ${stringResource(R.string.curr)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Balance Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏦", fontSize = 32.sp)
                        Text(stringResource(R.string.stat_balance), color = Color.White, fontSize = 12.sp)
                        Text(
                            "${balance.toInt()} ${stringResource(R.string.curr)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recently Paid Section
            Text(
                stringResource(R.string.dash_table_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val recentPayments = remember(appData) {
                // Find apartments that have at least one payment in the payments map
                val paidAptIds = appData.payments.keys
                    .mapNotNull { it.split("_").firstOrNull()?.toLongOrNull() }
                    .toSet()

                appData.apartments
                    .filter { it.id in paidAptIds && it.lastPaidTime > 0 }
                    .sortedByDescending { it.lastPaidTime }
                    .take(3)
            }
            
            if (recentPayments.isNotEmpty()) {
                recentPayments.forEach { apt ->
                    val bld = appData.buildings.find { it.id == apt.buildingId }
                    Card(
                        modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(apt.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(bld?.name ?: "", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("✅", fontSize = 16.sp)
                                val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                    .format(java.util.Date(apt.lastPaidTime))
                                Text(date, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_apts), color = Color.Gray, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly Stats Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.title_stats_month),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                // Small Month Selector for this section
                var monthExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = it }
                ) {
                    OutlinedTextField(
                        value = months[selectedMonth - 1],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.height(20.dp)) },
                        modifier = Modifier
                            .menuAnchor()
                            .width(100.dp)
                            .height(45.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        months.forEachIndexed { index, month ->
                            DropdownMenuItem(
                                text = { Text(month, fontSize = 12.sp) },
                                onClick = {
                                    selectedMonth = index + 1
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly stats grid
            val buildingStats = remember(appData, selectedMonth, selectedYear) {
                appData.buildings.map { building ->
                    val buildingApts = appData.apartments.filter { it.buildingId == building.id }
                    val totalApts = buildingApts.size
                    val paidCount = buildingApts.count { apt ->
                        val amount = appData.payments["${apt.id}_${selectedYear}_${selectedMonth}"]
                        amount != null && amount > 0
                    }
                    Triple(building.name, paidCount, totalApts)
                }.filter { it.third > 0 }
            }

            if (buildingStats.isNotEmpty()) {
                Column {
                    buildingStats.forEach { (name, paid, total) ->
                        val color = when {
                            paid == total -> Color(0xFF10B981)
                            paid > 0 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏢 $name", fontWeight = FontWeight.Bold)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$paid / $total",
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "${stringResource(R.string.txt_paid_month)} ${months[selectedMonth - 1]}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.no_apts),
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToFullPayments,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.btn_view_all))
            }
        }
    }

}

