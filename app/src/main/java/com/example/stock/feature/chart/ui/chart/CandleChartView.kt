package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleEntry

/**
 * MPAndroidChartを使用してローソク足チャートを表示するComposable。
 *
 * @param modifier レイアウトモディファイア
 * @param entries ローソク足エントリーのリスト
 * @param lows Y軸範囲計算用の安値リスト
 * @param highs Y軸範囲計算用の高値リスト
 * @param onReady チャート初期化時に呼び出されるコールバック
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