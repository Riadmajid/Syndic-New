package com.example.syndic.ui.screens

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
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
import com.example.syndic.R
import com.example.syndic.viewmodel.SyndicViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SyndicViewModel,
    onNavigateToFullPayments: () -> Unit
) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    var selectedMonth by remember { mutableIntStateOf(currentMonth) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var monthFilter by remember { mutableStateOf("all") }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showChangePwdDialog by remember { mutableStateOf(false) }

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val years = (currentYear - 2..currentYear + 5).toList()

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
                                imageVector = Icons.Default.Settings,
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
                                    com.example.syndic.utils.LocaleHelper.setLocale("ar")
                                    langExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Français") },
                                onClick = {
                                    com.example.syndic.utils.LocaleHelper.setLocale("fr")
                                    langExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    com.example.syndic.utils.LocaleHelper.setLocale("en")
                                    langExpanded = false
                                }
                            )
                        }
                    }

                    if (isAuthenticated) {
                        IconButton(onClick = { showChangePwdDialog = true }) {
                            Text("🔑", fontSize = 20.sp)
                        }
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

    if (showChangePwdDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        ChangePasswordDialog(
            onConfirm = { old, new, conf ->
                if (old != appData.password) {
                    android.widget.Toast.makeText(context, R.string.alert_wrong_pwd, android.widget.Toast.LENGTH_SHORT).show()
                } else if (new.isBlank() || new != conf) {
                    android.widget.Toast.makeText(context, R.string.alert_pwd_mismatch, android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.changePassword(old, new)
                    android.widget.Toast.makeText(context, R.string.alert_pwd_changed, android.widget.Toast.LENGTH_SHORT).show()
                    showChangePwdDialog = false
                }
            },
            onDismiss = { showChangePwdDialog = false }
        )
    }
}
