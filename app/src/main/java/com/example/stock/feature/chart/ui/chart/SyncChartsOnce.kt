package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart

/**
 * ローソク足チャートと出来高チャートを一度だけ同期させるComposable。
 *
 * 2つのチャートインスタンスが揃ったタイミングで、
 * スクロールやズームなどの操作を連動させる。
 *
 * @param candle ローソク足チャートのインスタンス
 * @param volume 出来高チャートのインスタンス
 */
@Composable
fun SyncChartsOnce(candle: CandleStickChart?, volume: BarChart?) {
    LaunchedEffect(candle, volume) {
        // 両方のチャートがnullでなければ同期処理を実行
        if (candle != null && volume != null) {
            attachSynchronizedPair(candle, volume)
        }
    }
}