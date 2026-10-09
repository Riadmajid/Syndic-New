package com.example.syndic.zaineb4.ui.screens

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Apartments : Screen("apartments")
    object Expenses : Screen("expenses")
    object Communication : Screen("communication")
    object FullPayments : Screen("full_payments")
    object PendingApproval : Screen("pending_approval")
    object Admin : Screen("admin")
}
