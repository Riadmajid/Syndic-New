package com.example.syndic.ui.screens

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.syndic.R
import com.example.syndic.data.Apartment
import com.example.syndic.viewmodel.SyndicViewModel
import java.util.Calendar

// Auth Dialog
@Composable
fun AuthDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

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
                Text("[AUTH]", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.auth_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    stringResource(R.string.auth_desc),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.ph_pwd)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConfirm(password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_confirm))
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

// Change Password Dialog
@Composable
fun ChangePasswordDialog(
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
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
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text(stringResource(R.string.ph_old_pwd)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                        onClick = { onConfirm(oldPwd, newPwd, confPwd) },
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
    isBuilding: Boolean,
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
                Text("[DELETE]", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.modal_del_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Red
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    if (isBuilding) stringResource(R.string.msg_del_bld) else stringResource(R.string.msg_del_default),
                    fontWeight = FontWeight.Medium,
                    color = if (isBuilding) Color.Red else Color.DarkGray
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
                Text("[PAYMENT]", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

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
    viewModel: SyndicViewModel,
    onShowResidentInfo: () -> Unit,
    onMonthClick: (Int, Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableStateOf(currentYear) }

    val years = (currentYear - 2..currentYear + 5).toList()
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

                    Button(
                        onClick = onShowResidentInfo
                    ) {
                        Text(stringResource(R.string.btn_show_resident))
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
                            if (apartment.residentPhone.isNotBlank()) {
                                Text(
                                    "${stringResource(R.string.lbl_res_phone)} ${apartment.residentPhone}",
                                    fontWeight = FontWeight.Medium
                                )
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

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onMonthClick(month, selectedYear) },
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
                                    if (isPaid) "${amount.toInt()}" else stringResource(R.string.unpaid),
                                    fontSize = 10.sp,
                                    color = if (isPaid) Color.White else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
