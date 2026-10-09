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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.syndic.zaineb4.R
import com.example.syndic.zaineb4.data.Apartment
import com.example.syndic.zaineb4.data.Building
import com.example.syndic.zaineb4.data.UserAccount
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel
import java.util.Calendar

/**
 * Checks if a given apartment belongs to the logged-in resident using multiple matching strategies.
 */
fun isApartmentOfResident(apt: Apartment, buildingName: String?, user: UserAccount?): Boolean {
    if (user == null || user.role == "admin") return false
    val uApt = user.apartment.trim()
    val uName = user.name.trim()
    val uBld = user.building.trim()
    val aName = apt.name.trim()
    val aResName = apt.residentName.trim()

    // 1. Exact match on apartment name
    if (uApt.isNotEmpty() && aName.equals(uApt, ignoreCase = true)) return true

    // 2. Exact match on resident name
    if (uName.isNotEmpty() && aResName.isNotEmpty() && aResName.equals(uName, ignoreCase = true)) return true

    // 3. Containment on resident name
    if (uName.isNotEmpty() && aResName.isNotEmpty() && (
        aResName.contains(uName, ignoreCase = true) || uName.contains(aResName, ignoreCase = true)
    )) return true

    // 4. Containment on apartment name
    if (uApt.isNotEmpty() && aName.isNotEmpty() && (
        aName.contains(uApt, ignoreCase = true) || uApt.contains(aName, ignoreCase = true)
    )) return true

    // 5. Compare digits (e.g. "Appartement 4" vs "4" or "شقة 4")
    val uDigits = uApt.filter { it.isDigit() }
    val aDigits = aName.filter { it.isDigit() }
    if (uDigits.isNotEmpty() && uDigits == aDigits) {
        if (uBld.isEmpty() || buildingName.isNullOrEmpty() ||
            buildingName.contains(uBld, ignoreCase = true) || uBld.contains(buildingName, ignoreCase = true)
        ) {
            return true
        }
    }

    return false
}

fun findResidentApartment(
    apartments: List<Apartment>,
    buildings: List<Building>,
    user: UserAccount?
): Apartment? {
    if (user == null || user.role == "admin") return null
    return apartments.find { apt ->
        val bld = buildings.find { it.id == apt.buildingId }
        isApartmentOfResident(apt, bld?.name, user)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentsScreen(viewModel: SyndicViewModel) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val isAdmin = userAccount?.role == "admin"

    // Robust matching for resident's apartment
    val residentApartment = remember(appData.apartments, appData.buildings, userAccount) {
        findResidentApartment(appData.apartments, appData.buildings, userAccount)
    }

    var selectedBuildingId by remember { mutableLongStateOf(0L) }
    var selectedApartmentId by remember { mutableLongStateOf(0L) }

    // Auto-navigate resident to their own apartment
    androidx.compose.runtime.LaunchedEffect(residentApartment?.id) {
        if (!isAdmin && residentApartment != null) {
            selectedBuildingId = residentApartment.buildingId
            selectedApartmentId = residentApartment.id
        }
    }

    var showAddBuildingDialog by remember { mutableStateOf(false) }
    var showAddApartmentDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showResidentDialog by remember { mutableStateOf(false) }

    var renameId by remember { mutableLongStateOf(0L) }
    var renameType by remember { mutableStateOf("") }
    var renameCurrentName by remember { mutableStateOf("") }

    var showLogoutDialog by remember { mutableStateOf(false) }

    var deleteId by remember { mutableLongStateOf(0L) }
    var deleteType by remember { mutableStateOf("") }
    var deleteIsBuilding by remember { mutableStateOf(false) }

    var paymentMonth by remember { mutableIntStateOf(0) }
    var paymentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (2014..2040).toList()

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when {
                        selectedApartmentId != 0L -> {
                            val apt = appData.apartments.find { it.id == selectedApartmentId }
                            Text("🏠 ${apt?.name ?: ""}")
                        }
                        selectedBuildingId != 0L -> {
                            val bld = appData.buildings.find { it.id == selectedBuildingId }
                            Text("🏢 ${bld?.name ?: ""}")
                        }
                        else -> Text(stringResource(R.string.nav_apartments))
                    }
                },
                navigationIcon = {
                    when {
                        selectedApartmentId != 0L -> {
                            IconButton(onClick = { selectedApartmentId = 0L }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back_apts))
                            }
                        }
                        selectedBuildingId != 0L -> {
                            IconButton(onClick = { selectedBuildingId = 0L }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back_blds))
                            }
                        }
                    }
                },
                actions = {
                    if (isAuthenticated) {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                when {
                    selectedApartmentId != 0L -> {} // No FAB in apartment details
                    selectedBuildingId != 0L -> {
                        FloatingActionButton(onClick = { showAddApartmentDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_apt))
                        }
                    }
                    else -> {
                        FloatingActionButton(onClick = { showAddBuildingDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_bld))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                selectedApartmentId != 0L -> {
                    // Apartment Details Screen
                    val apt = appData.apartments.find { it.id == selectedApartmentId }
                    if (apt != null) {
                        val bld = appData.buildings.find { it.id == apt.buildingId }
                        val isResidentOfThisApt = !isAdmin && userAccount != null && (
                            apt.id == residentApartment?.id ||
                            isApartmentOfResident(apt, bld?.name, userAccount)
                        )
                        ApartmentDetailsScreen(
                            apartment = apt,
                            buildingName = bld?.name ?: "",
                            viewModel = viewModel,
                            isAdmin = isAdmin,
                            isResidentOfThisApartment = isResidentOfThisApt,
                            onShowResidentInfo = { showResidentDialog = true },
                            onMonthClick = { month, year ->
                                paymentMonth = month
                                paymentYear = year
                                showPaymentDialog = true
                            }
                        )
                    } else {
                        selectedApartmentId = 0L
                    }
                }
                selectedBuildingId != 0L -> {
                    // Building Apartments Screen
                    val buildingApts = appData.apartments.filter { it.buildingId == selectedBuildingId }
                    val bld = appData.buildings.find { it.id == selectedBuildingId }
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
                        Column {
                            buildingApts.forEach { apt ->
                                val isMyApt = !isAdmin && userAccount != null && (
                                    apt.id == residentApartment?.id ||
                                    isApartmentOfResident(apt, bld?.name, userAccount)
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedApartmentId = apt.id },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMyApt) Color(0xFFF0FDF4) else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🏠 ${apt.name}", fontWeight = FontWeight.Bold)
                                                if (isMyApt) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "شقتك 📥",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF16A34A)
                                                    )
                                                }
                                            }
                                            if (apt.residentName.isNotBlank()) {
                                                Text(apt.residentName, fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        if (isAdmin) {
                                            Row {
                                                IconButton(onClick = {
                                                    renameId = apt.id
                                                    renameType = "apartment"
                                                    renameCurrentName = apt.name
                                                    showRenameDialog = true
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit))
                                                }
                                                 IconButton(onClick = {
                                                     deleteId = apt.id
                                                     deleteType = "apartment"
                                                     deleteIsBuilding = false
                                                     showDeleteDialog = true
                                                 }) {
                                                     Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.btn_delete), tint = Color.Red)
                                                 }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Buildings List Screen
                    if (!isAdmin && userAccount != null) {
                        if (residentApartment != null) {
                            val myBld = appData.buildings.find { it.id == residentApartment.buildingId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clickable {
                                        selectedBuildingId = residentApartment.buildingId
                                        selectedApartmentId = residentApartment.id
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                elevation = CardDefaults.cardElevation(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("🏠 شقتي الخاصة: ${residentApartment.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("🏢 ${myBld?.name ?: ""}", fontSize = 12.sp, color = Color.DarkGray)
                                    }
                                    Text("📥 عرض الوصولات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ℹ️ حسابك مسجل باسم: ${userAccount!!.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                                    Text("الشقة: ${userAccount!!.apartment} • العمارة: ${userAccount!!.building}", fontSize = 12.sp, color = Color(0xFF78350F))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("اختر عمارتك وشقتك من القائمة أدناه لعرض وصولات الأداء الخاصة بك.", fontSize = 11.sp, color = Color(0xFFB45309))
                                }
                            }
                        }
                    }
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
                        Column {
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🏢 ${building.name}", fontWeight = FontWeight.Bold)
                                        if (isAdmin) {
                                            Row {
                                                IconButton(onClick = {
                                                    renameId = building.id
                                                    renameType = "building"
                                                    renameCurrentName = building.name
                                                    showRenameDialog = true
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit))
                                                }
                                                 IconButton(onClick = {
                                                     deleteId = building.id
                                                     deleteType = "building"
                                                     deleteIsBuilding = true
                                                     showDeleteDialog = true
                                                 }) {
                                                     Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.btn_delete), tint = Color.Red)
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
    }


    if (showAddBuildingDialog) {
        AddBuildingDialog(
            onConfirm = { name ->
                viewModel.addBuilding(name)
                showAddBuildingDialog = false
            },
            onDismiss = { showAddBuildingDialog = false }
        )
    }

    if (showAddApartmentDialog) {
        AddApartmentDialog(
            onConfirm = { name ->
                viewModel.addApartment(selectedBuildingId, name)
                showAddApartmentDialog = false
            },
            onDismiss = { showAddApartmentDialog = false }
        )
    }

    if (showRenameDialog) {
        RenameDialog(
            currentName = renameCurrentName,
            onConfirm = { newName ->
                when (renameType) {
                    "building" -> {
                        val building = appData.buildings.find { it.id == renameId }
                        building?.let { viewModel.updateBuilding(it.copy(name = newName)) }
                    }
                    "apartment" -> {
                        val apt = appData.apartments.find { it.id == renameId }
                        apt?.let { viewModel.updateApartment(it.copy(name = newName)) }
                    }
                }
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            isBuilding = deleteIsBuilding,
            onConfirm = {
                when (deleteType) {
                    "building" -> viewModel.deleteBuilding(deleteId)
                    "apartment" -> viewModel.deleteApartment(deleteId)
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showPaymentDialog) {
        val hasReceipt = appData.paymentReceipts["${selectedApartmentId}_${paymentYear}_${paymentMonth}"] == true
        PaymentDialog(
            month = months[paymentMonth - 1],
            monthNumber = paymentMonth,
            year = paymentYear,
            apartmentId = selectedApartmentId,
            currentAmount = viewModel.getPaymentAmount(selectedApartmentId, paymentYear, paymentMonth),
            hasExistingReceipt = hasReceipt,
            viewModel = viewModel,
            onSave = { amount, base64Image, removeReceipt ->
                viewModel.savePaymentWithReceipt(
                    selectedApartmentId,
                    paymentYear,
                    paymentMonth,
                    amount,
                    base64Image,
                    removeReceipt
                )
                showPaymentDialog = false
            },
            onClear = {
                viewModel.clearPayment(selectedApartmentId, paymentYear, paymentMonth)
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    if (showResidentDialog) {
        val apt = appData.apartments.find { it.id == selectedApartmentId }
        apt?.let {
            ResidentDialog(
                apartment = it,
                onSave = { name, phone ->
                    viewModel.updateApartment(it.copy(residentName = name, residentPhone = phone))
                    showResidentDialog = false
                },
                onDismiss = { showResidentDialog = false }
            )
        }
    }
}

