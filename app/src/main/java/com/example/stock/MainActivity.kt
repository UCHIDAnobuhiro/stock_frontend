package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.stock.navigation.AppNavGraph
import com.example.stock.ui.AuthViewModelFactory
import com.example.stock.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authVm = ViewModelProvider(this, AuthViewModelFactory(this))[AuthViewModel::class.java]
        setContent {
            AppNavGraph(authVm)
        }
    }
}
