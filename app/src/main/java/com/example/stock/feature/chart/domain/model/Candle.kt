package com.example.stock.feature.chart.domain.model

/**
 * ローソク足データポイントを表すドメインエンティティ。
 *
 * @property time 日付文字列（例："2024-01-15"）
 * @property open 始値
 * @property high 高値
 * @property low 安値
 * @property close 終値
 * @property volume 出来高
 */
data class Candle(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
