package com.example.syndic.ui.screens

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Apartments : Screen("apartments")
    data object Expenses : Screen("expenses")
    data object Communication : Screen("communication")
    data object FullPayments : Screen("full_payments")
}
