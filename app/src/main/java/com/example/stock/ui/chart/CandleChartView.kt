package com.example.stock.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.CandleEntry

/**
 * ローソク足チャート（CandleStickChart）を表示するComposable。
 *
 * MPAndroidChartのCandleStickChartをCompose上でラップし、
 * データ・ラベル・スタイル・軸設定・初期化コールバックを提供する。
 *
 * @param modifier レイアウト修飾子
 * @param entries ローソク足データリスト
 * @param lows 各ローソク足の安値リスト
 * @param highs 各ローソク足の高値リスト
 * @param onReady チャート初期化時のコールバック（CandleStickChartインスタンスを渡す）
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
            // CandleStickChartインスタンス生成・初期化
            CandleStickChart(ctx).apply {
                applyCandleDefaults() // チャートのデフォルト設定
                onReady(this)         // 初期化コールバック
            }
        },
        update = { chart ->
            chart.fitScreen() // ズーム・スクロール状態をリセット
            if (entries.isEmpty()) {
                showLoading(chart, chart.context); return@AndroidView // データが無ければローディング表示
            }
            updateOrCreateCandleData(chart, entries) // データセット更新
            // X軸設定
            setupXAxisCommon(chart, entries.size)
            // Y軸設定
            setupRightAxisForCandle(chart, lows, highs)
            // データ更新を通知
            chart.notifyDataSetChanged()
            chart.axisRight.refreshGridLimitLinesFromAxis()
            chart.isAutoScaleMinMaxEnabled = false
            chart.isScaleYEnabled = false

            chart.highlightValues(null) // ハイライト解除
            chart.invalidate()         // 再描画
        }
    )
}