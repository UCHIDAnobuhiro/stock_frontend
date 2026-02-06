package com.example.stock.feature.chart.domain.model

/**
 * Domain entity representing a candlestick data point.
 *
 * @property time Date string (e.g., "2024-01-15")
 * @property open Opening price
 * @property high Highest price
 * @property low Lowest price
 * @property close Closing price
 * @property volume Trading volume
 */
data class Candle(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
