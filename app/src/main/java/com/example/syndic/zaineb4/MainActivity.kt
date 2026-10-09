package com.example.syndic.zaineb4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.syndic.zaineb4.repository.SyndicRepository
import com.example.syndic.zaineb4.ui.components.BottomNavBar
import com.example.syndic.zaineb4.ui.screens.ApartmentsScreen
import com.example.syndic.zaineb4.ui.screens.CommunicationScreen
import com.example.syndic.zaineb4.ui.screens.DashboardScreen
import com.example.syndic.zaineb4.ui.screens.ExpensesScreen
import com.example.syndic.zaineb4.ui.screens.FullPaymentsScreen
import com.example.syndic.zaineb4.ui.screens.AdminScreen
import com.example.syndic.zaineb4.ui.screens.LoginScreen
import com.example.syndic.zaineb4.ui.screens.PendingApprovalScreen
import com.example.syndic.zaineb4.ui.screens.Screen
import com.example.syndic.zaineb4.ui.theme.SYNDICTheme
import com.example.syndic.zaineb4.utils.NotificationHelper
import com.example.syndic.zaineb4.viewmodel.SyndicViewModel

class MainActivity : AppCompatActivity() {

    private val repository = SyndicRepository()

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle response if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)
        repository.setContext(this)
        repository.startListening()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SYNDICTheme {
                SyndicApp(repository)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.stopListening()
    }
}

@Composable
fun SyndicApp(repository: SyndicRepository) {
    val navController = rememberNavController()
    val viewModel: SyndicViewModel = viewModel(
        factory = SyndicViewModel.provideFactory(repository)
    )

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isAuthenticated, userAccount) {
        if (isAuthenticated) {
            if (userAccount?.approved == true) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    popUpTo(Screen.PendingApproval.route) { inclusive = true }
                }
            } else if (userAccount != null) {
                navController.navigate(Screen.PendingApproval.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isAuthenticated && userAccount?.approved == true) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) {
                if (userAccount?.approved == true) Screen.Dashboard.route else Screen.PendingApproval.route
            } else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(viewModel = viewModel)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFullPayments = {
                        navController.navigate(Screen.FullPayments.route)
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Screen.Admin.route)
                    }
                )
            }
            composable(Screen.Apartments.route) {
                ApartmentsScreen(viewModel = viewModel)
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(viewModel = viewModel)
            }
            composable(Screen.Communication.route) {
                CommunicationScreen(viewModel = viewModel)
            }
            composable(Screen.FullPayments.route) {
                FullPaymentsScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.PendingApproval.route) {
                PendingApprovalScreen(viewModel = viewModel)
            }
            composable(Screen.Admin.route) {
                AdminScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
