package com.example.stock.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry

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
            chart.fitScreen()
            if (entries.isEmpty()) {
                showLoading(chart, chart.context); return@AndroidView
            }
            updateOrCreateBarData(chart, entries)
            // X軸
            setupXAxisCommon(chart, entries.size)
            chart.xAxis.valueFormatter = makeDateAxisFormatter(labels)
            chart.xAxis.labelRotationAngle = ChartTokens.Dimens.X_LABEL_ROTATION
            // Y軸
            setupRightAxisForVolume(chart, entries)
            // データ更新を通知
            chart.notifyDataSetChanged()
            chart.axisRight.refreshGridLimitLinesFromAxis()

            chart.highlightValues(null)
            chart.invalidate()
        }
    )
}