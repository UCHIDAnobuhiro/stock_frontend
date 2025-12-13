package com.example.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stock.core.ui.theme.StockTheme
import com.example.stock.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity that serves as the application entry point.
 * Sets up the navigation graph with Hilt-injected ViewModels.
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Initialization processing when the Activity is created.
     * Sets up the navigation graph and builds the UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Set up the application navigation with Hilt-injected ViewModels
            StockTheme {
                AppNavGraph()
            }
        }
    }
}
