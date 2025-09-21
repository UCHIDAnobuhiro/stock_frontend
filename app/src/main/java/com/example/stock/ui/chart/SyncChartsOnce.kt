package com.example.stock.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart

@Composable
fun SyncChartsOnce(candle: CandleStickChart?, volume: BarChart?) {
    LaunchedEffect(candle, volume) {
        if (candle != null && volume != null) {
            attachSynchronizedPair(candle, volume)
        }
    }
}