package com.example.stock.feature.chart.ui

/**
 * Candlestick data model for UI display.
 *
 * Instead of directly passing the DTO from the API response to the UI,
 * a lightweight data structure formatted for UI consumption is defined.
 *
 * @property time Date and time (e.g., "2025-11-03")
 * @property open Opening price
 * @property high High price
 * @property low Low price
 * @property close Closing price
 * @property volume Trading volume
 */
data class CandleItem(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
