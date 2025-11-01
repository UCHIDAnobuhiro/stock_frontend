package com.example.stock.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.ui.component.MainHeader
import com.example.stock.ui.theme.Spacing
import com.example.stock.ui.theme.Thickness
import com.example.stock.viewmodel.SymbolViewModel


/**
 * 銘柄リスト画面。
 *
 * APIから取得した銘柄一覧をリスト表示し、
 * タップでチャート画面へ遷移できる。
 *
 * @param navController ナビゲーション操作用コントローラ
 * @param vm 銘柄リスト取得用ViewModel
 * @param onLogout ログアウト時のコールバック
 */
@Composable
fun StockListScreen(
    navController: NavController,
    vm: SymbolViewModel,
    onLogout: () -> Unit,
) {
    // 銘柄リストのStateFlowを購読
    val state by vm.ui.collectAsState()
    // 初回表示時にリスト取得
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            MainHeader(
                titleText = stringResource(R.string.app_header_stock_list),
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(Spacing.Screen)

        when {
            // 1) 読み込み中
            state.isLoading -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 2) エラー
            state.error != null -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(
                            modifier = Modifier.padding(top = Spacing.ListItemVertical)
                        )
                        Button(onClick = { vm.load() }) {
                            Text(stringResource(R.string.reload))
                        }
                    }
                }
            }

            // 3) 空データ
            state.symbols.isEmpty() -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_symbols))
                }
            }

            // 銘柄リスト表示
            else -> {
                Column(modifier) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(
                            state.symbols,
                            key = { _, item -> item.code }
                        ) { index, stock ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // タップでチャート画面へ遷移
                                    .clickable {
                                        navController.navigate("chart/${stock.name}/${stock.code}")
                                    }
                                    .background(Color.Transparent)
                                    .padding(vertical = Spacing.ListItemVertical),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 銘柄名
                                Text(text = stock.name, style = MaterialTheme.typography.bodyLarge)
                                // 銘柄コード
                                Text(text = stock.code, style = MaterialTheme.typography.bodyLarge)
                            }
                            if (index < state.symbols.lastIndex) {
                                HorizontalDivider(thickness = Thickness.Divider)
                            }
                        }
                    }
                }
            }
        }
    }
}