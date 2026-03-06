package com.example.syndic

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
import com.example.syndic.repository.SyndicRepository
import com.example.syndic.ui.components.BottomNavBar
import com.example.syndic.ui.screens.ApartmentsScreen
import com.example.syndic.ui.screens.CommunicationScreen
import com.example.syndic.ui.screens.DashboardScreen
import com.example.syndic.ui.screens.ExpensesScreen
import com.example.syndic.ui.screens.FullPaymentsScreen
import com.example.syndic.ui.screens.Screen
import com.example.syndic.ui.theme.SYNDICTheme
import com.example.syndic.utils.NotificationHelper
import com.example.syndic.viewmodel.SyndicViewModel

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFullPayments = {
                        navController.navigate(Screen.FullPayments.route)
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
        }
    }
}