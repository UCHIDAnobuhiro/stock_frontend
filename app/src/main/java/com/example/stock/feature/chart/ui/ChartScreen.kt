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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.feature.chart.ui.chart.CandleChartView
import com.example.stock.feature.chart.ui.chart.SyncChartsOnce
import com.example.stock.feature.chart.ui.chart.VolumeChartView
import com.example.stock.core.ui.component.CommonHeader
import com.example.stock.core.ui.component.IntervalDropDown
import com.example.stock.core.ui.theme.Sizes
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry

/**
 * Screen displaying candlestick and volume charts for a stock.
 *
 * @param navController Navigation controller
 * @param name Stock name
 * @param code Stock symbol code
 * @param vm ViewModel for candlestick data
 * @param onLogout Callback for logout action
 */
@Composable
fun ChartScreen(
    navController: NavController,
    name: String,
    code: String,
    vm: CandlesViewModel,
    onLogout: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    val dataAsc = remember(ui.items) { ui.items.sortedBy { it.time } }
    val labels = remember(dataAsc) { dataAsc.map { it.time } }
    var interval by remember { mutableStateOf("1day") }

    LaunchedEffect(code, interval) { vm.load(code, interval = interval, outputsize = 200) }
    DisposableEffect(Unit) { onDispose { vm.clear() } }

    var candleChartRef by remember { mutableStateOf<CandleStickChart?>(null) }
    var volumeChartRef by remember { mutableStateOf<BarChart?>(null) }

    Scaffold(
        topBar = {
            CommonHeader(
                titleText = stringResource(R.string.app_header_candle_chart),
                onBack = { navController.popBackStack() },
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
                onSelected = { interval = it }
            )

            Column(Modifier.fillMaxSize()) {
                SyncChartsOnce(candleChartRef, volumeChartRef)

                CandleChartView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f, fill = true),
                    entries = dataAsc.mapIndexed { i, c ->
                        CandleEntry(
                            i.toFloat(),
                            c.high.toFloat(),
                            c.low.toFloat(),
                            c.open.toFloat(),
                            c.close.toFloat()
                        )
                    },
                    lows = dataAsc.map { it.low },
                    highs = dataAsc.map { it.high },
                    onReady = { candleChartRef = it }
                )

                VolumeChartView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f, fill = true),
                    entries = dataAsc.mapIndexed { i, c ->
                        BarEntry(
                            i.toFloat(),
                            c.volume.toFloat()
                        )
                    },
                    labels = labels,
                    onReady = { volumeChartRef = it }
                )

            }
        }
    }
}