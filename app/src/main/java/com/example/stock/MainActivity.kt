package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.stock.feature.stocklist.data.repository.StockRepository
import com.example.stock.navigation.AppNavGraph
import com.example.stock.feature.auth.viewmodel.AuthViewModelFactory
import com.example.stock.feature.chart.viewmodel.CandlesViewModelFactory
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModelFactory
import com.example.stock.core.ui.theme.StockTheme
import com.example.stock.feature.auth.viewmodel.AuthViewModel
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel

/**
 * Activity that serves as the application entry point.
 * Initializes each ViewModel and sets up the navigation graph.
 */
class MainActivity : ComponentActivity() {
    /**
     * Repository for fetching stock price data. Initialized lazily when needed.
     */
    private val stockRepo by lazy { StockRepository() }

    /**
     * ViewModel for managing authentication.
     */
    private val authVm by viewModels<AuthViewModel> {
        AuthViewModelFactory(applicationContext)
    }

    /**
     * ViewModel for managing the stock symbol list.
     */
    private val symbolVm by viewModels<SymbolViewModel> {
        SymbolViewModelFactory(stockRepo)
    }

    /**
     * ViewModel for managing candlestick chart data.
     */
    private val candlesVm by viewModels<CandlesViewModel> {
        CandlesViewModelFactory(stockRepo)
    }

    /**
     * Initialization processing when the Activity is created.
     * Sets up the navigation graph and builds the UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Set up the application navigation
            StockTheme {
                AppNavGraph(authVm, symbolVm, candlesVm)
            }
        }
    }
}
