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
            chart.fitScreen()
            if (entries.isEmpty()) {
                showLoading(chart, chart.context); return@AndroidView
            }
            updateOrCreateCandleData(chart, entries)
            // X軸
            setupXAxisCommon(chart, entries.size)
            // Y軸
            setupRightAxisForCandle(chart, lows, highs)
            // データ更新を通知
            chart.notifyDataSetChanged()
            chart.axisRight.refreshGridLimitLinesFromAxis()
            chart.isAutoScaleMinMaxEnabled = false
            chart.isScaleYEnabled = false

            chart.highlightValues(null)
            chart.invalidate()
        }
    )
}