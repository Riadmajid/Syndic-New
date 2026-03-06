package com.example.syndic.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.content.pm.PackageManager
import android.os.Build
import com.example.syndic.R
import com.example.syndic.data.Expense
import com.example.syndic.viewmodel.SyndicViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: SyndicViewModel) {
    val appData by viewModel.appData.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    var selectedMonth by remember { mutableStateOf("all") }
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedExpenseId by remember { mutableLongStateOf(0L) }
    var selectedExpenseHasReceipt by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var selectedBase64 by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    
    // Image selection logic
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val photoUri = remember {
        try {
            val file = File(context.cacheDir, "temp_receipt.jpg")
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun uriToBase64(uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options) 
            }

            // Calculate inSampleSize to downsample if necessary
            var inSampleSize = 1
            val maxDimension = 1200
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // Decode with inSampleSize
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            
            val bitmap = contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options) 
            } ?: return null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                val base64 = uriToBase64(uri)
                if (base64 != null) {
                    if (selectedExpenseId != 0L) {
                        viewModel.saveReceiptImage(selectedExpenseId, base64) {}
                    } else {
                        // For new expense
                        selectedBase64 = base64
                    }
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = uriToBase64(it)
            if (base64 != null) {
                if (selectedExpenseId != 0L) {
                    viewModel.saveReceiptImage(selectedExpenseId, base64) {}
                } else {
                    selectedBase64 = base64
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        // For camera, we only need CAMERA and potentially external storage if saving there
        // For gallery, it depends on API level
    }

    fun checkAndLaunchCamera() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            photoUri?.let { cameraLauncher.launch(it) }
        } else {
            permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
        }
    }

    fun checkAndLaunchGallery() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(arrayOf(permission))
        }
    }

    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val years = (currentYear - 2..currentYear + 5).toList()

    // Filter expenses
    val filteredExpenses = remember(appData.expenses, selectedMonth, selectedYear) {
        appData.expenses.filter { expense ->
            val dateParts = expense.date.split("-")
            if (dateParts.size >= 3) {
                val expYear = dateParts[0].toIntOrNull()
                val expMonth = dateParts[1].toIntOrNull()
                if (expYear != null && expMonth != null) {
                    if (expYear == selectedYear) {
                        val filterMonth = selectedMonth.toIntOrNull()
                        filterMonth == null || expMonth == filterMonth
                    } else false
                } else true
            } else true
        }.sortedByDescending { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_expenses)) },
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
            // Add Expense Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.title_new_exp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)) }
                    var desc by remember { mutableStateOf("") }
                    var amount by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(stringResource(R.string.th_date)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(stringResource(R.string.lbl_desc)) },
                        placeholder = { Text(stringResource(R.string.ph_exp_desc)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(stringResource(R.string.lbl_amount)) },
                        placeholder = { Text(stringResource(R.string.ph_amount)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedExpenseId = 0L
                                showImageSourceDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (selectedBase64 == null) stringResource(R.string.btn_add_img) else stringResource(R.string.btn_edit_img))
                        }
                        
                        if (selectedBase64 != null) {
                            Text(stringResource(R.string.txt_selected), color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            IconButton(onClick = { selectedBase64 = null }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isAuthenticated) {
                                if (desc.isNotBlank() && amount.isNotBlank()) {
                                    val amt = amount.toDoubleOrNull()
                                    if (amt != null && amt > 0) {
                                        val expense = Expense(
                                            id = System.currentTimeMillis(),
                                            desc = desc,
                                            amount = amt,
                                            date = date,
                                            hasReceipt = selectedBase64 != null
                                        )
                                        viewModel.addExpenseWithReceipt(expense, selectedBase64)
                                        
                                        desc = ""
                                        amount = ""
                                        selectedBase64 = null
                                    }
                                }
                            } else {
                                showAuthDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_add_exp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Expenses List
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.title_exp_list),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Month Filter
                    var monthExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (selectedMonth == "all") stringResource(R.string.opt_all_months) else months[selectedMonth.toInt() - 1],
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
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.opt_all_months), fontSize = 12.sp) },
                                onClick = {
                                    selectedMonth = "all"
                                    monthExpanded = false
                                }
                            )
                            months.forEachIndexed { index, month ->
                                DropdownMenuItem(
                                    text = { Text(month, fontSize = 12.sp) },
                                    onClick = {
                                        selectedMonth = (index + 1).toString()
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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
                                .height(45.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("$year", fontSize = 12.sp) },
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

            if (filteredExpenses.isEmpty()) {
                Text(
                    stringResource(R.string.no_apts),
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                filteredExpenses.forEach { expense ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    expense.date,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Text(expense.desc, fontWeight = FontWeight.Medium)
                                Text(
                                    "${expense.amount} ${stringResource(R.string.curr)}",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (expense.hasReceipt) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedExpenseId = expense.id
                                            selectedExpenseHasReceipt = expense.hasReceipt
                                            showReceiptDialog = true
                                        },
                                        modifier = Modifier.height(36.dp).padding(0.dp)
                                    ) {
                                        Text(stringResource(R.string.btn_view_receipt), fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { /* Download logic placeholder */ },
                                        modifier = Modifier.height(36.dp).padding(0.dp)
                                    ) {
                                        Text("⬇️", fontSize = 10.sp)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        stringResource(R.string.no_receipt),
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                    if (isAuthenticated) {
                                        TextButton(
                                            onClick = { 
                                                selectedExpenseId = expense.id
                                                showImageSourceDialog = true
                                            },
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(stringResource(R.string.btn_add_img), fontSize = 10.sp) 
                                        }
                                    }
                                }
                            }

                            if (isAuthenticated) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        selectedExpenseId = expense.id
                                        showDeleteDialog = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Red
                                    )
                                ) {
                                    Text(stringResource(R.string.btn_delete))
                                }
                            }
                        }
                    }
                }
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

    if (showDeleteDialog) {
        DeleteDialog(
            isBuilding = false,
            onConfirm = {
                viewModel.deleteExpense(selectedExpenseId)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showReceiptDialog) {
        ReceiptDialog(
            expenseId = selectedExpenseId,
            viewModel = viewModel,
            onDismiss = { showReceiptDialog = false }
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
                    Text(stringResource(R.string.title_img_source), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            checkAndLaunchCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_camera))
                    }
                    
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            checkAndLaunchGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_gallery))
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
fun ReceiptDialog(
    expenseId: Long,
    viewModel: SyndicViewModel,
    onDismiss: () -> Unit
) {
    val appData by viewModel.appData.collectAsState()
    // In a real app, we might want to fetch the image separately if it's large
    // For now, it's in the same document or a separate collection we'd need to fetch
    // Repository.saveReceiptImage uses receiptsCollection.document(expenseId.toString())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.th_receipt),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                var loading by remember { mutableStateOf(true) }

                LaunchedEffect(expenseId) {
                    loading = true
                    val db = FirebaseFirestore.getInstance()
                    db.collection("syndic_receipts").document(expenseId.toString()).get()
                        .addOnSuccessListener { snapshot ->
                            var base64 = snapshot.getString("image")
                            if (base64 != null) {
                                try {
                                    // Strip data URL prefix if present
                                    if (base64!!.startsWith("data:")) {
                                        val commaIndex = base64!!.indexOf(",")
                                        if (commaIndex != -1) {
                                            base64 = base64!!.substring(commaIndex + 1)
                                        }
                                    }
                                    val decodedString = Base64.decode(base64, Base64.DEFAULT)
                                    bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            loading = false
                        }
                        .addOnFailureListener {
                            loading = false
                        }
                }

                if (loading) {
                    Box(modifier = Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Receipt Image",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(stringResource(R.string.err_load_img))
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    }
}

