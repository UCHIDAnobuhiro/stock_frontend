package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleEntry

/**
 * Composable that displays a candlestick chart using MPAndroidChart.
 *
 * @param modifier Layout modifier
 * @param entries List of candlestick entries
 * @param lows List of low prices for Y-axis range calculation
 * @param highs List of high prices for Y-axis range calculation
 * @param onReady Callback invoked when the chart is initialized
 */
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
                showLoading(chart, chart.context)
                return@AndroidView
            }
            updateOrCreateCandleData(chart, entries)
            setupXAxisCommon(chart, entries.size)
            setupRightAxisForCandle(chart, lows, highs)
            chart.notifyDataSetChanged()
            chart.axisRight.refreshGridLimitLinesFromAxis()
            chart.isAutoScaleMinMaxEnabled = false
            chart.isScaleYEnabled = false
            chart.highlightValues(null)
            chart.invalidate()
        }
    )
}