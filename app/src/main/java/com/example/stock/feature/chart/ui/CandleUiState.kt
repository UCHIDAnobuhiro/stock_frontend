package com.example.stock.feature.chart.ui

import androidx.annotation.StringRes

/**
 * UI state for the candlestick chart screen.
 *
 * Represents the current state of the chart display,
 * including loading status, fetched candle data, and error information.
 *
 * @property isLoading Whether the candle data is currently being loaded
 * @property items List of candle items for chart display
 * @property errorResId String resource ID for error message, or null if no error
 */
data class CandleUiState(
    val isLoading: Boolean = false,
    val items: List<CandleItem> = emptyList(),
    @StringRes val errorResId: Int? = null
)
