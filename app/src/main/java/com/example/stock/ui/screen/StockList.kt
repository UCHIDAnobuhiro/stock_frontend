package com.example.stock.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.ui.component.MainHeader
import com.example.stock.viewmodel.SymbolViewModel


/**
 * @param navController ナビゲーション操作を行うためのコントローラ。
 */
@Composable
fun StockListScreen(
    navController: NavController,
    vm: SymbolViewModel,
    onLogout: () -> Unit,
) {
    val stocks by vm.symbols.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            MainHeader(
                titleText = stringResource(R.string.app_header_stock_list),
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(stocks) { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("chart/${stock.name}/${stock.code}")
                            }
                            .background(Color.Transparent)
                            .padding(vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stock.name, fontSize = 18.sp)
                        Text(text = stock.code, fontSize = 18.sp)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}