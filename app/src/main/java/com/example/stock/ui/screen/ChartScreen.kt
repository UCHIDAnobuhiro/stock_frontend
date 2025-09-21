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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stock.R
import com.example.stock.ui.chart.ChartTokens
import com.example.stock.ui.chart.applyCandleDefaults
import com.example.stock.ui.chart.applyVolumeDefaults
import com.example.stock.ui.chart.attachSynchronizedPair
import com.example.stock.ui.chart.makeDateAxisFormatter
import com.example.stock.ui.chart.refreshGridLimitLines
import com.example.stock.ui.chart.setupRightAxisForCandle
import com.example.stock.ui.chart.setupRightAxisForVolume
import com.example.stock.ui.chart.setupXAxisCommon
import com.example.stock.ui.chart.showLoading
import com.example.stock.ui.chart.updateOrCreateBarData
import com.example.stock.ui.chart.updateOrCreateCandleData
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
                var synced by remember { mutableStateOf(false) }
                LaunchedEffect(code) { synced = false }
                if (!synced && candleChartRef != null && volumeChartRef != null) {
                    SideEffect {
                        attachSynchronizedPair(candleChartRef!!, volumeChartRef!!)
                        synced = true
                    }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(3f),
                    factory = { ctx ->
                        CandleStickChart(ctx).apply {
                            applyCandleDefaults()
                            candleChartRef = this
                        }
                    },
                    update = { chart ->
                        // データをエントリーに変更
                        val entries = dataAsc.mapIndexed { i, c ->
                            CandleEntry(
                                i.toFloat(),
                                c.high.toFloat(),
                                c.low.toFloat(),
                                c.open.toFloat(),
                                c.close.toFloat()
                            )
                        }

                        //データが空の場合はローディング表示
                        if (entries.isEmpty()) {
                            showLoading(chart, chart.context)
                            return@AndroidView
                        }

                        // データセット更新（既存があれば再利用）
                        updateOrCreateCandleData(chart, entries)

                        // X軸を設定
                        setupXAxisCommon(chart, entries.size)

                        // Y軸を設定
                        setupRightAxisForCandle(
                            chart,
                            lows = dataAsc.map { it.low },
                            highs = dataAsc.map { it.high }
                        )
                        // Y軸の補助線を設定
                        chart.axisRight.refreshGridLimitLines()

                        // 縦方向のスケールを固定（ズーム禁止）
                        chart.isAutoScaleMinMaxEnabled = false
                        chart.isScaleYEnabled = false

                        // 再描画
                        chart.invalidate()
                    }
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f),
                    factory = { ctx ->
                        BarChart(ctx).apply {
                            applyVolumeDefaults()
                            volumeChartRef = this
                        }
                    },
                    update = { chart ->
                        // データをエントリーに変更
                        val volEntries = dataAsc.mapIndexed { i, c ->
                            BarEntry(i.toFloat(), c.volume.toFloat())
                        }

                        //データが空の場合はローディング表示
                        if (volEntries.isEmpty()) {
                            showLoading(chart, chart.context)
                            return@AndroidView
                        }

                        // データセット更新（既存があれば再利用）
                        updateOrCreateBarData(chart, volEntries)

                        // X軸を設定
                        setupXAxisCommon(chart, volEntries.size)
                        chart.xAxis.valueFormatter = makeDateAxisFormatter(labels)
                        chart.xAxis.labelRotationAngle = ChartTokens.Dimens.X_LABEL_ROTATION

                        // Y軸を設定
                        setupRightAxisForVolume(chart, volEntries)
                        // Y軸の補助線を設定
                        chart.axisRight.refreshGridLimitLines()

                        // 再描画
                        chart.invalidate()
                    }
                )
            }
        }
    }
}