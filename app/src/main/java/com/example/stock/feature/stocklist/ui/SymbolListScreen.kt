package com.example.stock.feature.stocklist.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stock.R
import com.example.stock.core.ui.component.MainHeader
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.core.ui.theme.Thickness
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel

/**
 * ViewModelを使用した銘柄リスト画面。
 *
 * [SymbolViewModel]と[SymbolListScreenContent]を接続するラッパーComposable。
 * 状態監視と初期データ読み込みを処理する。
 * HiltによりSymbolViewModelが自動的に注入される。
 *
 * @param onNavigateToChart 銘柄名とコードを持ってチャート画面へ遷移するコールバック
 * @param onLogout ログアウトボタン押下時に呼び出されるコールバック
 * @param viewModel 銘柄リストViewModel（Hiltにより注入）
 */
@Composable
fun SymbolListScreen(
    onNavigateToChart: (name: String, code: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: SymbolViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()

    // 初回表示時に銘柄リストを読み込む
    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    SymbolListScreenContent(
        uiState = uiState,
        onSymbolClick = onNavigateToChart,
        onReload = viewModel::load,
        onLogout = onLogout
    )
}

/**
 * ステートレスな銘柄リスト画面コンテンツ。
 *
 * APIから取得した銘柄リストを表示する。
 * 必要に応じてローディングインジケーター、リトライボタン付きエラー状態、空状態を表示する。
 *
 * @param uiState 現在のUI状態
 * @param onSymbolClick 銘柄アイテムがクリックされた時のコールバック
 * @param onReload 銘柄リストを再読み込みするコールバック
 * @param onLogout ログアウトボタン押下時のコールバック
 */
@Composable
fun SymbolListScreenContent(
    uiState: SymbolUiState,
    onSymbolClick: (name: String, code: String) -> Unit,
    onReload: () -> Unit,
    onLogout: () -> Unit,
) {
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
            // 読み込み中
            uiState.isLoading -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // エラー状態
            uiState.errorResId != null -> {
                Box(
                    modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(uiState.errorResId), color = MaterialTheme.colorScheme.error)
                        Spacer(
                            modifier = Modifier.padding(top = Spacing.ListItemVertical)
                        )
                        Button(onClick = onReload) {
                            Text(stringResource(R.string.reload))
                        }
                    }
                }
            }

            // 空状態
            uiState.symbols.isEmpty() -> {
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
                            uiState.symbols,
                            key = { _, item -> item.code }
                        ) { index, symbol ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // タップでチャート画面へ遷移
                                    .clickable {
                                        onSymbolClick(symbol.name, symbol.code)
                                    }
                                    .background(Color.Transparent)
                                    .padding(vertical = Spacing.ListItemVertical),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 銘柄名
                                Text(text = symbol.name, style = MaterialTheme.typography.bodyLarge)
                                // 銘柄コード
                                Text(text = symbol.code, style = MaterialTheme.typography.bodyLarge)
                            }
                            if (index < uiState.symbols.lastIndex) {
                                HorizontalDivider(thickness = Thickness.Divider)
                            }
                        }
                    }
                }
            }
        }
    }
}
