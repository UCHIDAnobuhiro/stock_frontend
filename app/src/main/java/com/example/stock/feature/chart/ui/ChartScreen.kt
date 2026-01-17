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
import androidx.hilt.navigation.compose.hiltViewModel
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
 * Chart screen with ViewModel.
 *
 * Wrapper composable that connects [CandlesViewModel] to [ChartScreenContent].
 * Handles state observation and data loading.
 * Uses Hilt to automatically inject the CandlesViewModel.
 *
 * @param name Stock name
 * @param code Stock symbol code
 * @param onNavigateBack Callback to navigate back to previous screen
 * @param onLogout Callback invoked when logout button is pressed
 * @param viewModel Candles ViewModel (injected by Hilt)
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
 * Stateless chart screen content.
 *
 * Displays candlestick and volume charts for a stock.
 * Shows stock name, code, interval selector, and synchronized charts.
 *
 * @param uiState Current UI state
 * @param name Stock name
 * @param code Stock symbol code
 * @param interval Current interval selection
 * @param onIntervalChange Callback when interval selection changes
 * @param onNavigateBack Callback to navigate back
 * @param onLogout Callback when logout button is pressed
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
