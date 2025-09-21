package com.example.stock.ui.screen

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.ui.chart.CandleChartView
import com.example.stock.ui.chart.SyncChartsOnce
import com.example.stock.ui.chart.VolumeChartView
import com.example.stock.ui.component.CommonHeader
import com.example.stock.viewmodel.CandlesViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry

@Composable
fun ChartScreen(
    navController: NavController,
    name: String,
    code: String,
    vm: CandlesViewModel,
    onLogout: () -> Unit
) {
    val candles by vm.candles.collectAsStateWithLifecycle()

    val dataAsc = remember(candles) { candles.sortedBy { it.time } }
    val labels = remember(dataAsc) { dataAsc.map { it.time } }

    LaunchedEffect(code) { vm.load(code, interval = "1day", outputsize = 200) }
    DisposableEffect(Unit) { onDispose { vm.clear() } }

    // チャート参照を保持
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = colorResource(R.color.border),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp),
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
            Column(Modifier.fillMaxSize()) {
                // 同期
                SyncChartsOnce(candleChartRef, volumeChartRef)

                // Candle Compose
                CandleChartView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
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

                // Volume Compose
                VolumeChartView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f),
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