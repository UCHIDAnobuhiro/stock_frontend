package com.example.stock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.login.LoginScreen
import com.example.stock.feature.auth.ui.signup.SignupScreen
import com.example.stock.feature.chart.ui.ChartScreen
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.example.stock.feature.stocklist.ui.StockListScreen
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

// Routes are identifiers for screen navigation.
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val STOCK = "stock"
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthRepositoryEntryPoint {
    fun authRepository(): AuthRepository
}

/**
 * Composable that defines the navigation graph for the entire application.
 */
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val authRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthRepositoryEntryPoint::class.java
        ).authRepository()
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
            val symbolViewModel: SymbolViewModel = hiltViewModel()
            StockListScreen(
                navController,
                symbolViewModel,
                onLogout = {
                    coroutineScope.launch {
                        authRepository.logout()
                    }
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("chart/{name}/{code}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: return@composable
            val code = backStackEntry.arguments?.getString("code") ?: return@composable
            val candlesViewModel: CandlesViewModel = hiltViewModel()
            ChartScreen(
                navController,
                name,
                code,
                candlesViewModel,
                onLogout = {
                    coroutineScope.launch {
                        authRepository.logout()
                    }
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }
    }
}
