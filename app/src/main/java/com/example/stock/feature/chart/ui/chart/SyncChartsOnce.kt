package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart

/**
 * ローソク足チャートと出来高チャートの両方が準備完了したら同期するComposable。
 *
 * @param candle ローソク足チャートインスタンス
 * @param volume 出来高チャートインスタンス
 */
@Composable
fun SyncChartsOnce(candle: CandleStickChart?, volume: BarChart?) {
    LaunchedEffect(candle, volume) {
        if (candle != null && volume != null) {
            attachSynchronizedPair(candle, volume)
        }
    }
}