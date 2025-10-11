package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.stock.data.repository.StockRepository
import com.example.stock.navigation.AppNavGraph
import com.example.stock.ui.factory.AuthViewModelFactory
import com.example.stock.ui.factory.CandlesViewModelFactory
import com.example.stock.ui.factory.SymbolViewModelFactory
import com.example.stock.viewmodel.AuthViewModel
import com.example.stock.viewmodel.CandlesViewModel
import com.example.stock.viewmodel.SymbolViewModel

class MainActivity : ComponentActivity() {
    private val stockRepo by lazy { StockRepository() }

    private val authVm by viewModels<AuthViewModel> {
        AuthViewModelFactory(applicationContext)
    }
    private val symbolVm by viewModels<SymbolViewModel> {
        SymbolViewModelFactory(stockRepo)
    }
    private val candlesVm by viewModels<CandlesViewModel> {
        CandlesViewModelFactory(stockRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavGraph(authVm, symbolVm, candlesVm)
        }
    }
}
