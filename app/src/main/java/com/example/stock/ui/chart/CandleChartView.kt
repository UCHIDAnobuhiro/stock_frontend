package com.example.stock.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleEntry

@Composable
fun CandleChartView(
    modifier: Modifier = Modifier,
    entries: List<CandleEntry>,
    lows: List<Double>,
    highs: List<Double>,
    onReady: (CandleStickChart) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                applyCandleDefaults()
                onReady(this)
            }
        },
        update = { chart ->
            if (entries.isEmpty()) {
                showLoading(chart, chart.context); return@AndroidView
            }
            updateOrCreateCandleData(chart, entries)
            setupXAxisCommon(chart, entries.size)
            setupRightAxisForCandle(chart, lows, highs)
            chart.axisRight.refreshGridLimitLines()
            chart.isAutoScaleMinMaxEnabled = false
            chart.isScaleYEnabled = false
            chart.invalidate()
        }
    )
}