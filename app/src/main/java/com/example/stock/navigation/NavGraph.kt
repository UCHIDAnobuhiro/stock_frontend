package com.example.stock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stock.feature.auth.ui.login.LoginScreen
import com.example.stock.feature.auth.ui.signup.SignupScreen
import com.example.stock.feature.auth.viewmodel.LogoutViewModel
import com.example.stock.feature.chart.ui.ChartScreen
import com.example.stock.feature.stocklist.ui.SymbolListScreen

// Routes are identifiers for screen navigation.
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val STOCK = "stock"
    const val CHART = "chart/{name}/{code}"

    fun chart(name: String, code: String): String {
        val encodedName = android.net.Uri.encode(name)
        val encodedCode = android.net.Uri.encode(code)
        return "chart/$encodedName/$encodedCode"
    }
}

/**
 * Composable that defines the navigation graph for the entire application.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val logoutViewModel: LogoutViewModel = hiltViewModel()

    // Observe logout events and navigate to login screen
    LaunchedEffect(Unit) {
        logoutViewModel.events.collect { event ->
            when (event) {
                LogoutViewModel.UiEvent.LoggedOut -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    // Navigate to stock list screen on successful login and clear the back stack so user cannot return to login
                    navController.navigate(Routes.STOCK) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignedUp = {
                    // Navigate back to login screen on successful signup
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.STOCK) {
            SymbolListScreen(
                onNavigateToChart = { name, code ->
                    navController.navigate(Routes.chart(name, code))
                },
                onLogout = { logoutViewModel.logout() }
            )
        }
        composable(Routes.CHART) { backStackEntry ->
            // Navigation component automatically decodes URI parameters
            val name = backStackEntry.arguments?.getString("name") ?: return@composable
            val code = backStackEntry.arguments?.getString("code") ?: return@composable
            ChartScreen(
                name = name,
                code = code,
                onNavigateBack = { navController.popBackStack() },
                onLogout = { logoutViewModel.logout() }
            )
        }
    }
}
