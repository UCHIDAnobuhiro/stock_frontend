package com.example.stock.feature.chart.ui.chart

/** Design tokens for chart configuration. */
object ChartTokens {
    object Dimens {
        // Candlestick chart viewport offsets
        const val CANDLE_LEFT = 20f
        const val CANDLE_TOP = 20f
        const val CANDLE_RIGHT = 70f
        const val CANDLE_BOTTOM = 20f

        // Volume chart viewport offsets
        const val VOLUME_LEFT = 20f
        const val VOLUME_TOP = 10f
        const val VOLUME_RIGHT = 70f
        const val VOLUME_BOTTOM = 80f

        // Number of visible candles
        const val VISIBLE_COUNT = 60f

        // X-axis grid line interval
        const val X_STRIDE = 10

        // X-axis label rotation angle
        const val X_LABEL_ROTATION = -25f

        // X-axis margin to prevent clipping
        const val X_BOUND_MARGIN = 0.5f

        // Y-axis padding ratio for candlestick range
        const val Y_PAD_RATIO = 0.05f

        // Number of Y-axis labels
        const val Y_LABEL_COUNT = 5
    }
}