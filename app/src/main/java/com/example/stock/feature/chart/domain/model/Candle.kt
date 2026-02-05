package com.example.stock.feature.chart.domain.model

data class Candle(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
