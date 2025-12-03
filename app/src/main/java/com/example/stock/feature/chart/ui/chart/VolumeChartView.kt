package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry

/**
 * 出来高チャート（BarChart）を表示するComposable。
 *
 * MPAndroidChartのBarChartをCompose上でラップし、
 * データ・ラベル・スタイル・軸設定・初期化コールバックを提供する。
 *
 * @param modifier レイアウト修飾子
 * @param entries 棒グラフのデータリスト
 * @param labels X軸ラベル（日付など）
 * @param onReady チャート初期化時のコールバック（BarChartインスタンスを渡す）
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
            // BarChartインスタンス生成・初期化
            BarChart(ctx).apply {
                applyVolumeDefaults() // チャートのデフォルト設定
                onReady(this)         // 初期化コールバック
            }
        },
        update = { chart ->
            if (entries.isEmpty()) {
                showLoading(chart, chart.context); return@AndroidView // データが無ければローディング表示
            }
            updateOrCreateBarData(chart, entries) // データセット更新
            // X軸設定
            setupXAxisCommon(chart, entries.size)
            chart.xAxis.valueFormatter = makeDateAxisFormatter(labels)
            chart.xAxis.labelRotationAngle = ChartTokens.Dimens.X_LABEL_ROTATION
            // Y軸設定
            setupRightAxisForVolume(chart, entries)
            // データ更新を通知
            chart.notifyDataSetChanged()
            chart.axisRight.refreshGridLimitLinesFromAxis()

            chart.highlightValues(null) // ハイライト解除
            chart.invalidate()         // 再描画
        }
    )
}