package com.example.stock.feature.chart.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stock.R
import com.example.stock.core.ui.component.CommonHeader
import com.example.stock.core.ui.component.IntervalDropDown
import com.example.stock.core.ui.theme.Sizes
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.feature.chart.ui.chart.CandleChartView
import com.example.stock.feature.chart.ui.chart.SyncChartsOnce
import com.example.stock.feature.chart.ui.chart.VolumeChartView
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry

/**
 * ViewModelを使用したチャート画面。
 *
 * [CandlesViewModel]と[ChartScreenContent]を接続するラッパーComposable。
 * 状態監視とデータ読み込みを処理する。
 * HiltによりCandlesViewModelが自動的に注入される。
 *
 * @param name 銘柄名
 * @param code 銘柄コード
 * @param onNavigateBack 前の画面へ戻るコールバック
 * @param onLogout ログアウトボタン押下時に呼び出されるコールバック
 * @param viewModel ローソク足ViewModel（Hiltにより注入）
 */
@Composable
fun ChartScreen(
    name: String,
    code: String,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CandlesViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()
    var interval by remember { mutableStateOf("1day") }

    LaunchedEffect(code, interval) {
        viewModel.load(code, interval = interval, outputsize = 200)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clear() }
    }

    ChartScreenContent(
        uiState = uiState,
        name = name,
        code = code,
        interval = interval,
        onIntervalChange = { interval = it },
        onNavigateBack = onNavigateBack,
        onLogout = onLogout
    )
}

/**
 * ステートレスなチャート画面コンテンツ。
 *
 * 銘柄のローソク足チャートと出来高チャートを表示する。
 * 銘柄名、コード、間隔セレクター、同期されたチャートを表示する。
 *
 * @param uiState 現在のUI状態
 * @param name 銘柄名
 * @param code 銘柄コード
 * @param interval 現在の間隔選択
 * @param onIntervalChange 間隔選択が変更された時のコールバック
 * @param onNavigateBack 戻るコールバック
 * @param onLogout ログアウトボタン押下時のコールバック
 */
@Composable
fun ChartScreenContent(
    uiState: CandleUiState,
    name: String,
    code: String,
    interval: String,
    onIntervalChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val dataAsc = remember(uiState.items) { uiState.items.sortedBy { it.time } }
    val labels = remember(dataAsc) { dataAsc.map { it.time } }
    val candleEntries = remember(dataAsc) {
        dataAsc.mapIndexed { i, c ->
            CandleEntry(
                i.toFloat(),
                c.high.toFloat(),
                c.low.toFloat(),
                c.open.toFloat(),
                c.close.toFloat()
            )
        }
    }
    val volumeEntries = remember(dataAsc) {
        dataAsc.mapIndexed { i, c ->
            BarEntry(i.toFloat(), c.volume.toFloat())
        }
    }
    val lows = remember(dataAsc) { dataAsc.map { it.low } }
    val highs = remember(dataAsc) { dataAsc.map { it.high } }

    var candleChartRef by remember { mutableStateOf<CandleStickChart?>(null) }
    var volumeChartRef by remember { mutableStateOf<BarChart?>(null) }

    Scaffold(
        topBar = {
            CommonHeader(
                titleText = stringResource(R.string.app_header_candle_chart),
                onBack = onNavigateBack,
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.Screen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = Sizes.Border,
                        color = colorResource(R.color.border),
                        shape = RoundedCornerShape(Sizes.CornerSm)
                    )
                    .padding(Spacing.Screen),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = code,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            IntervalDropDown(
                selected = interval,
                onSelected = onIntervalChange
            )

            Column(Modifier.fillMaxSize()) {
                SyncChartsOnce(candleChartRef, volumeChartRef)

                CandleChartView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f, fill = true),
                    entries = candleEntries,
                    lows = lows,
                    highs = highs,
                    onReady = { candleChartRef = it }
                )

                VolumeChartView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f, fill = true),
                    entries = volumeEntries,
                    labels = labels,
                    onReady = { volumeChartRef = it }
                )
            }
        }
    }
}
