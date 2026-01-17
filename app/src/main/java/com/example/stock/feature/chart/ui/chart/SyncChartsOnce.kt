package com.example.stock.feature.chart.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart

/**
 * Composable that synchronizes candlestick and volume charts once both are ready.
 *
 * @param candle Candlestick chart instance
 * @param volume Volume chart instance
 */
@Composable
fun SyncChartsOnce(candle: CandleStickChart?, volume: BarChart?) {
    LaunchedEffect(candle, volume) {
        if (candle != null && volume != null) {
            attachSynchronizedPair(candle, volume)
        }
    }
}