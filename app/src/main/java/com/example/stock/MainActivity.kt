package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.stock.navigation.AppNavGraph
import com.example.stock.ui.AuthViewModelFactory
import com.example.stock.ui.SymbolViewModelFactory
import com.example.stock.viewmodel.AuthViewModel
import com.example.stock.viewmodel.SymbolViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authVm = ViewModelProvider(this, AuthViewModelFactory(this))[AuthViewModel::class.java]
        val symbolVm = ViewModelProvider(this, SymbolViewModelFactory())[SymbolViewModel::class.java]
        setContent {
            AppNavGraph(authVm,symbolVm)
        }
    }
}
