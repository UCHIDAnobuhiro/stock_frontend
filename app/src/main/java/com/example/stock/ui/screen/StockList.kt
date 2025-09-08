package com.example.stock.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.ui.component.StockListHeader


/**
 * @param navController ナビゲーション操作を行うためのコントローラ。
 */
@Composable
fun StockListScreen(navController: NavController, onLogout: () -> Unit) {
    var title by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            StockListHeader(
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.app_header_text))
        }
    }
}