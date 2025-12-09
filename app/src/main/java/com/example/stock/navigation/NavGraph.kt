package com.example.stock.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stock.feature.auth.ui.login.LoginScreen
import com.example.stock.feature.auth.ui.signup.SignupScreen
import com.example.stock.feature.auth.viewmodel.LoginViewModel
import com.example.stock.feature.auth.viewmodel.SignupViewModel
import com.example.stock.feature.chart.ui.ChartScreen
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.example.stock.feature.stocklist.ui.StockListScreen
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel

// Routes are identifiers for screen navigation.
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val STOCK = "stock"
}

/**
 * Composable that defines the navigation graph for the entire application.
 *
 * @param signupViewModel [SignupViewModel] that manages signup authentication state.
 * @param symbolViewModel [SymbolViewModel] that manages stock symbol list state.
 * @param candlesViewModel [CandlesViewModel] that manages candlestick chart data state.
 */
@Composable
fun AppNavGraph(
    signupViewModel: SignupViewModel,
    symbolViewModel: SymbolViewModel,
    candlesViewModel: CandlesViewModel
) {
    val navController = rememberNavController()

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
                signupViewModel,
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
            val loginViewModel: LoginViewModel = hiltViewModel()
            StockListScreen(
                navController,
                symbolViewModel,
                onLogout = {
                    // Perform logout process
                    loginViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("chart/{name}/{code}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: return@composable
            val code = backStackEntry.arguments?.getString("code") ?: return@composable
            val loginViewModel: LoginViewModel = hiltViewModel()
            ChartScreen(
                navController,
                name,
                code,
                candlesViewModel,
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }
    }
}