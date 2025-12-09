package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.stock.feature.stocklist.data.repository.StockRepository
import com.example.stock.navigation.AppNavGraph
import com.example.stock.feature.auth.viewmodel.SignupViewModelFactory
import com.example.stock.feature.chart.viewmodel.CandlesViewModelFactory
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModelFactory
import com.example.stock.core.ui.theme.StockTheme
import com.example.stock.feature.auth.viewmodel.SignupViewModel
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity that serves as the application entry point.
 * Initializes each ViewModel and sets up the navigation graph.
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Repository for fetching stock price data. Initialized lazily when needed.
     */
    private val stockRepo by lazy { StockRepository() }

    /**
     * ViewModel for managing signup authentication.
     */
    private val signupVm by viewModels<SignupViewModel> {
        SignupViewModelFactory(applicationContext)
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
                AppNavGraph(signupVm, symbolVm, candlesVm)
            }
        }
    }
}
