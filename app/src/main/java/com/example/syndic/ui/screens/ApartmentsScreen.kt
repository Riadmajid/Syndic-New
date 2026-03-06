package com.example.syndic.ui.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.Settings
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
import com.example.syndic.R
import com.example.syndic.data.Apartment
import com.example.syndic.data.Building
import com.example.syndic.viewmodel.SyndicViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentsScreen(viewModel: SyndicViewModel) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    var selectedBuildingId by remember { mutableLongStateOf(0L) }
    var selectedApartmentId by remember { mutableLongStateOf(0L) }

    var showAddBuildingDialog by remember { mutableStateOf(false) }
    var showAddApartmentDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showResidentDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }

    var renameId by remember { mutableLongStateOf(0L) }
    var renameType by remember { mutableStateOf("") }
    var renameCurrentName by remember { mutableStateOf("") }

    var deleteId by remember { mutableLongStateOf(0L) }
    var deleteType by remember { mutableStateOf("") }
    var deleteIsBuilding by remember { mutableStateOf(false) }

    var paymentMonth by remember { mutableIntStateOf(0) }
    var paymentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 2..currentYear + 5).toList()

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun requireAuth(action: () -> Unit) {
        if (isAuthenticated) {
            action()
        } else {
            pendingAction = action
            showAuthDialog = true
        }
    }

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
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isAuthenticated) {
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
                        ApartmentDetailsScreen(
                            apartment = apt,
                            viewModel = viewModel,
                            onShowResidentInfo = { requireAuth { showResidentDialog = true } },
                            onMonthClick = { month, year ->
                                requireAuth {
                                    paymentMonth = month
                                    paymentYear = year
                                    showPaymentDialog = true
                                }
                            }
                        )
                    } else {
                        selectedApartmentId = 0L
                    }
                }
                selectedBuildingId != 0L -> {
                    // Building Apartments Screen
                    val buildingApts = appData.apartments.filter { it.buildingId == selectedBuildingId }
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
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { selectedApartmentId = apt.id },
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🏠 ${apt.name}", fontWeight = FontWeight.Bold)
                                        if (isAuthenticated) {
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
                                                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.btn_delete), tint = Color.Red)
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
                                        if (isAuthenticated) {
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
                                                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.btn_delete), tint = Color.Red)
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

    // Dialogs
    if (showAuthDialog) {
        AuthDialog(
            onConfirm = { pwd ->
                if (viewModel.verifyPassword(pwd)) {
                    showAuthDialog = false
                    pendingAction?.invoke()
                    pendingAction = null
                }
            },
            onDismiss = { showAuthDialog = false }
        )
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
        PaymentDialog(
            month = months[paymentMonth - 1],
            year = paymentYear,
            currentAmount = viewModel.getPaymentAmount(selectedApartmentId, paymentYear, paymentMonth),
            onSave = { amount ->
                viewModel.savePayment(selectedApartmentId, paymentYear, paymentMonth, amount)
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
