package com.example.stock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stock.ui.screen.ChartScreen
import com.example.stock.ui.screen.LoginScreen
import com.example.stock.ui.screen.StockListScreen
import com.example.stock.viewmodel.AuthViewModel
import com.example.stock.viewmodel.CandlesViewModel
import com.example.stock.viewmodel.SymbolViewModel

// Routesは、画面遷移の識別子です。
object Routes {
    const val LOGIN = "login"
    const val STOCK = "stock"
}

/**
 * アプリ全体のナビゲーショングラフを定義するComposable。
 *
 * @param authViewModel ログイン認証の状態管理を行う [AuthViewModel]。
 *
 */
@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
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
                authViewModel,
                onLoggedIn = {
                    // ログイン成功で一覧画面に遷移し、戻るでログインに戻れないように消す
                    navController.navigate(Routes.STOCK) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.STOCK) {
            StockListScreen(
                navController,
                symbolViewModel,
                onLogout = {
                    // ログアウト処理
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("chart/{code}") { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: return@composable
            ChartScreen(
                navController,
                code,
                candlesViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })
        }
    }
}