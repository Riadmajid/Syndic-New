package com.example.syndic.zaineb4.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.res.stringResource
import com.example.syndic.zaineb4.R
import com.example.syndic.zaineb4.utils.AuthPreferences
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: SyndicViewModel) {
    val context = LocalContext.current
    val authPrefs = remember { AuthPreferences(context) }

    var email by remember { mutableStateOf(authPrefs.savedEmail) }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(authPrefs.rememberMe) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    
    // Additional Registration Fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedBorderColor = Color(0xFF1E3C72),
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color(0xFF1E3C72),
        unfocusedLabelColor = Color.Gray,
        cursorColor = Color(0xFF1E3C72),
        focusedLeadingIconColor = Color(0xFF1E3C72),
        unfocusedLeadingIconColor = Color.Gray,
        focusedTrailingIconColor = Color(0xFF1E3C72),
        unfocusedTrailingIconColor = Color.Gray,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3C72),
                        Color(0xFF2A5298)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.White,
                    tonalElevation = 4.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.syndic.zaineb4.R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp)
                    )
                }

                Text(
                    text = "Bellouzou",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3C72),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TabRow(
                    selectedTabIndex = if (isRegisterMode) 1 else 0,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (isRegisterMode) 1 else 0]),
                            color = Color(0xFF1E3C72)
                        )
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Tab(
                        selected = !isRegisterMode,
                        onClick = { 
                            isRegisterMode = false
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text(stringResource(R.string.auth_login_title), color = if (!isRegisterMode) Color(0xFF1E3C72) else Color.Gray, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = isRegisterMode,
                        onClick = { 
                            isRegisterMode = true
                            errorMessage = null
                            successMessage = null
                        },
                        text = { Text(stringResource(R.string.auth_register_title), color = if (isRegisterMode) Color(0xFF1E3C72) else Color.Gray, fontWeight = FontWeight.Bold) }
                    )
                }

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.lbl_name)) },
                        placeholder = { Text(stringResource(R.string.ph_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.lbl_phone)) },
                        placeholder = { Text(stringResource(R.string.ph_phone)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = customTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text(stringResource(R.string.lbl_building)) },
                        placeholder = { Text(stringResource(R.string.ph_building)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text(stringResource(R.string.lbl_apartment)) },
                        placeholder = { Text(stringResource(R.string.ph_apartment)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = customTextFieldColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.lbl_email)) },
                    placeholder = { Text(stringResource(R.string.ph_email_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = customTextFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.lbl_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = customTextFieldColors
                )

                if (!isRegisterMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1E3C72), checkmarkColor = Color.White, uncheckedColor = Color.Gray)
                            )
                            Text(stringResource(R.string.lbl_remember_me), fontSize = 14.sp, color = Color.Black)
                        }
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text(stringResource(R.string.btn_forgot_password), fontSize = 14.sp)
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = Color(0xFF10B981),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            errorMessage = null
                            successMessage = null
                            if (isRegisterMode) {
                                if (name.isBlank() || phone.isBlank() || building.isBlank() || apartment.isBlank()) {
                                    isLoading = false
                                    errorMessage = context.getString(R.string.err_fill_auth)
                                } else {
                                    viewModel.register(email, password, name, phone, apartment, building) { success, error ->
                                        isLoading = false
                                        if (!success) {
                                            errorMessage = error ?: context.getString(R.string.err_reg_failed)
                                        }
                                    }
                                }
                            } else {
                                viewModel.login(email, password) { success, error ->
                                    isLoading = false
                                    if (success) {
                                        authPrefs.rememberMe = rememberMe
                                        authPrefs.savedEmail = if (rememberMe) email else ""
                                    } else {
                                        errorMessage = error ?: context.getString(R.string.err_auth_failed)
                                    }
                                }
                            }
                        } else {
                            errorMessage = context.getString(R.string.err_fill_auth)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3C72)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isRegisterMode) stringResource(R.string.btn_register) else stringResource(R.string.btn_login),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Removed the redundant text button since we use Tabs now
            }
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var isResetLoading by remember { mutableStateOf(false) }
        var resetError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(stringResource(R.string.title_reset_pwd)) },
            text = {
                Column {
                    Text(stringResource(R.string.desc_reset_pwd))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(stringResource(R.string.lbl_email)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = customTextFieldColors
                    )
                    if (resetError != null) {
                        Text(resetError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            isResetLoading = true
                            viewModel.resetPassword(resetEmail) { success, error ->
                                isResetLoading = false
                                if (success) {
                                    successMessage = context.getString(R.string.msg_reset_sent)
                                    showForgotPasswordDialog = false
                                } else {
                                    resetError = error ?: context.getString(R.string.err_reset_failed)
                                }
                            }
                        }
                    },
                    enabled = !isResetLoading
                ) {
                    if (isResetLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text(stringResource(R.string.btn_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}
