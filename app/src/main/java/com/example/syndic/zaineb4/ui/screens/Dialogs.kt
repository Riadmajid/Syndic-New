package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.syndic.zaineb4.R
import com.example.syndic.zaineb4.data.Apartment
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel
import com.example.syndic.zaineb4.utils.ReceiptHelper
import java.io.File
import java.util.Calendar

// Change Password Dialog
@Composable
fun ChangePasswordDialog(
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPwd by remember { mutableStateOf("") }
    var confPwd by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔑", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.change_pwd_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text(stringResource(R.string.ph_new_pwd)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confPwd,
                    onValueChange = { confPwd = it },
                    label = { Text(stringResource(R.string.ph_confirm_pwd)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onConfirm("", newPwd, confPwd) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
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
}

// Add Building Dialog
@Composable
fun AddBuildingDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    stringResource(R.string.btn_add_bld),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.ph_bld_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

// Add Apartment Dialog
@Composable
fun AddApartmentDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    stringResource(R.string.btn_add_apt),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.ph_apt_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

// Rename Dialog
@Composable
fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    stringResource(R.string.modal_rename_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.ph_rename)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

// Delete Dialog
@Composable
fun DeleteDialog(
    title: String? = null,
    message: String? = null,
    isBuilding: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🗑️", fontSize = 32.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    title ?: stringResource(R.string.modal_del_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    message ?: if (isBuilding) stringResource(R.string.msg_del_bld) else stringResource(R.string.msg_del_default),
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_yes_del))
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_undo))
                    }
                }
            }
        }
    }
}

// Payment Dialog
@Composable
fun PaymentDialog(
    month: String,
    monthNumber: Int,
    year: Int,
    apartmentId: Long,
    currentAmount: Double,
    hasExistingReceipt: Boolean = false,
    viewModel: SyndicViewModel,
    onSave: (amount: Double, base64Image: String?, removeReceipt: Boolean) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf(if (currentAmount > 0) currentAmount.toString() else "") }
    var selectedBase64 by remember { mutableStateOf<String?>(null) }
    var existingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var removeExistingReceipt by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var loadingReceipt by remember { mutableStateOf(false) }

    // Fetch existing receipt from Firebase if attached
    LaunchedEffect(apartmentId, year, monthNumber, hasExistingReceipt) {
        if (hasExistingReceipt) {
            loadingReceipt = true
            viewModel.getPaymentReceiptImage(apartmentId, year, monthNumber) { b64 ->
                if (!b64.isNullOrBlank()) {
                    try {
                        val cleanB64 = if (b64.startsWith("data:")) {
                            val comma = b64.indexOf(",")
                            if (comma != -1) b64.substring(comma + 1) else b64
                        } else b64
                        val bytes = Base64.decode(cleanB64, Base64.DEFAULT)
                        existingBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                loadingReceipt = false
            }
        }
    }

    // Camera photo URI
    val photoUri = remember {
        try {
            val file = File(context.cacheDir, "temp_pay_receipt.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                val base64 = ReceiptHelper.uriToBase64(context, uri)
                if (base64 != null) {
                    selectedBase64 = base64
                    removeExistingReceipt = false
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = ReceiptHelper.uriToBase64(context, it)
            if (base64 != null) {
                selectedBase64 = base64
                removeExistingReceipt = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            photoUri?.let { cameraLauncher.launch(it) }
        }
    }

    fun checkAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
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
        if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(arrayOf(permission))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💰", fontSize = 28.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${stringResource(R.string.pay_title)} $month - $year",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.lbl_amount)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Section
                Text(
                    text = "🧾 صورة التوصيل (Reçu de paiement) :",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Determine display bitmap: either newly selected or existing
                val currentDisplayBitmap = remember(selectedBase64, existingBitmap, removeExistingReceipt) {
                    if (selectedBase64 != null) {
                        try {
                            val bytes = Base64.decode(selectedBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    } else if (!removeExistingReceipt) {
                        existingBitmap
                    } else null
                }

                if (currentDisplayBitmap != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = currentDisplayBitmap.asImageBitmap(),
                                contentDescription = "Receipt Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showImageSourceDialog = true },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                                ) {
                                    Text("📷 تغيير", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val fileName = "Recu_Paiement_${year}_M${monthNumber}_${System.currentTimeMillis()}.jpg"
                                        val saved = ReceiptHelper.saveBitmapToDownloads(context, currentDisplayBitmap, fileName)
                                        if (saved) {
                                            Toast.makeText(context, "✅ تم حفظ التوصيل في مجلد التحميلات (Downloads)", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                                ) {
                                    Text("📥 تحميل", fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = {
                                        selectedBase64 = null
                                        removeExistingReceipt = true
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showImageSourceDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("📷 تصوير / إضافة التوصيل (اختياري)", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onSave(amt, selectedBase64, removeExistingReceipt)
                            } else {
                                onClear()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }

                    OutlinedButton(
                        onClick = onClear,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_clear))
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            }
        }
    }

    if (showImageSourceDialog) {
        Dialog(onDismissRequest = { showImageSourceDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.title_img_source), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            checkAndLaunchCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷 ${stringResource(R.string.btn_camera)}")
                    }

                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            checkAndLaunchGallery()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖼️ ${stringResource(R.string.btn_gallery)}")
                    }

                    TextButton(onClick = { showImageSourceDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            }
        }
    }
}

// Backward-compatible overload
@Composable
fun PaymentDialog(
    month: String,
    year: Int,
    currentAmount: Double,
    onSave: (Double) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(if (currentAmount > 0) currentAmount.toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${stringResource(R.string.pay_title)} $month - $year",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.lbl_amount)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onSave(amt)
                            } else {
                                onClear()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }

                    OutlinedButton(
                        onClick = onClear,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_clear))
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            }
        }
    }
}

// Resident Dialog
@Composable
fun ResidentDialog(
    apartment: Apartment,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(apartment.residentName) }
    var phone by remember { mutableStateOf(apartment.residentPhone) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    stringResource(R.string.title_edit_resident),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.ph_res_name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.ph_res_phone)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel_edit))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSave(name, phone)
                        }
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}

// Apartment Details Screen (inline in the dialog/screen area)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentDetailsScreen(
    apartment: Apartment,
    buildingName: String = "",
    viewModel: SyndicViewModel,
    isAdmin: Boolean,
    isResidentOfThisApartment: Boolean = false,
    onShowResidentInfo: () -> Unit,
    onMonthClick: (Int, Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableStateOf(currentYear) }
    val context = LocalContext.current
    val userAccount by viewModel.userAccount.collectAsState()

    val years = (2014..2040).toList()
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Resident Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.title_resident_info),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (isAdmin) {
                        Button(
                            onClick = onShowResidentInfo
                        ) {
                            Text(stringResource(R.string.btn_show_resident))
                        }
                    }
                }

                if (apartment.residentName.isNotBlank() || apartment.residentPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            if (apartment.residentName.isNotBlank()) {
                                Text(
                                    "${stringResource(R.string.lbl_res_name)} ${apartment.residentName}",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (apartment.residentPhone.isNotBlank() && isAdmin) {
                                val phoneContext = LocalContext.current
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${stringResource(R.string.lbl_res_phone)} ${apartment.residentPhone}",
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${apartment.residentPhone}"))
                                            phoneContext.startActivity(intent)
                                        }) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF10B981))
                                        }
                                        IconButton(onClick = {
                                            val cleanPhone = apartment.residentPhone.replace(Regex("[^0-9+]"), "")
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
                                            phoneContext.startActivity(intent)
                                        }) {
                                            Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Year Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.lbl_year),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedYear.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(120.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString()) },
                            onClick = {
                                selectedYear = year
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Months Grid (Manual grid to avoid nesting scroll issues)
        val appData by viewModel.appData.collectAsState()
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val month = index + 1
                        val amount = appData.payments["${apartment.id}_${selectedYear}_${month}"] ?: 0.0
                        val isPaid = amount > 0
                        val hasReceipt = appData.paymentReceipts["${apartment.id}_${selectedYear}_${month}"] == true
                        val resName = apartment.residentName.ifBlank { userAccount?.name ?: "" }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (isAdmin) Modifier.clickable { onMonthClick(month, selectedYear) }
                                    else if (isResidentOfThisApartment && isPaid) Modifier.clickable {
                                        ReceiptHelper.downloadPaymentReceipt(
                                            context = context,
                                            apartmentId = apartment.id,
                                            year = selectedYear,
                                            month = month,
                                            apartmentName = apartment.name,
                                            buildingName = buildingName,
                                            residentName = resName,
                                            amount = amount
                                        )
                                    }
                                    else Modifier
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPaid) Color(0xFF10B981) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    months[index],
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isPaid) Color.White else Color.Black
                                )
                                Text(
                                    if (isPaid) {
                                        if (hasReceipt) "${amount.toInt()} 🧾" else "${amount.toInt()}"
                                    } else stringResource(R.string.unpaid),
                                    fontSize = 10.sp,
                                    color = if (isPaid) Color.White else Color(0xFFEF4444)
                                )
                                // Download button — visible only for the resident of this apartment for paid months
                                if (isResidentOfThisApartment && isPaid) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    androidx.compose.material3.FilledTonalButton(
                                        onClick = {
                                            ReceiptHelper.downloadPaymentReceipt(
                                                context = context,
                                                apartmentId = apartment.id,
                                                year = selectedYear,
                                                month = month,
                                                apartmentName = apartment.name,
                                                buildingName = buildingName,
                                                residentName = resName,
                                                amount = amount
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 2.dp, vertical = 2.dp
                                        ),
                                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color.White.copy(alpha = 0.28f),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("📥", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                if (hasReceipt) "وصل 🧾" else "وصل",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
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

// Logout Confirmation Dialog
@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.logout_confirm_title)) },
        text = { Text(stringResource(R.string.logout_confirm_msg)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.logout_confirm_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.logout_confirm_no))
            }
        }
    )
}

// Excel Export Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportDialog(
    initialYear: Int,
    initialMonth: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int?) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (2014..2040).toList()
    val monthNames = listOf(
        "01 - Jan (يناير)",
        "02 - Fév (فبراير)",
        "03 - Mar (مارس)",
        "04 - Avr (أبريل)",
        "05 - Mai (ماي)",
        "06 - Juin (يونيو)",
        "07 - Juil (يوليوز)",
        "08 - Août (غشت)",
        "09 - Sep (شتنبر)",
        "10 - Oct (أكتوبر)",
        "11 - Nov (نونبر)",
        "12 - Déc (دجنبر)"
    )

    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }

    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📄", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dialog_export_excel_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Year
                Text(
                    text = stringResource(R.string.lbl_export_year),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = {
                                    selectedYear = y
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month
                Text(
                    text = stringResource(R.string.lbl_export_month),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedMonth == null) stringResource(R.string.opt_all_months)
                        else monthNames[selectedMonth!! - 1],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.opt_all_months)) },
                            onClick = {
                                selectedMonth = null
                                monthExpanded = false
                            }
                        )
                        monthNames.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedMonth = idx + 1
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onConfirm(selectedYear, selectedMonth) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("📥 ${stringResource(R.string.btn_export_download)}")
                    }
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
}

@Composable
fun ExcelExportDialog(
    initialYear: Int,
    initialMonth: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int?) -> Unit
) {
    PdfExportDialog(initialYear, initialMonth, onDismiss, onConfirm)
}

