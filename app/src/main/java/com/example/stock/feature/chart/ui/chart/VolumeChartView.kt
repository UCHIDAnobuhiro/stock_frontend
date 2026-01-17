package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry

/**
 * Composable that displays a volume bar chart using MPAndroidChart.
 *
 * @param modifier Layout modifier
 * @param entries List of bar entries representing volume data
 * @param labels List of date strings for X-axis labels
 * @param onReady Callback invoked when the chart is initialized
 */
@Composable
fun VolumeChartView(
    modifier: Modifier = Modifier,
    entries: List<BarEntry>,
    labels: List<String>,
    onReady: (BarChart) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            BarChart(ctx).apply {
                applyVolumeDefaults()
                onReady(this)
            }
        },
        update = { chart ->
            if (entries.isEmpty()) {
                showLoading(chart, chart.context)
                return@AndroidView
            }
            updateOrCreateBarData(chart, entries)
            setupXAxisCommon(chart, entries.size)
            chart.xAxis.valueFormatter = makeDateAxisFormatter(labels)
            chart.xAxis.labelRotationAngle = ChartTokens.Dimens.X_LABEL_ROTATION
            setupRightAxisForVolume(chart, entries)
            chart.notifyDataSetChanged()
            chart.axisRight.refreshGridLimitLinesFromAxis()
            chart.highlightValues(null)
            chart.invalidate()
        }
    )
}