package com.example.stock.feature.chart.ui

/**
 * UI表示用のローソク足データモデル。
 *
 * APIレスポンスのDTOを直接UIに渡すのではなく、
 * UI向けにフォーマットされた軽量なデータ構造を定義する。
 *
 * @property time 日時（例："2025-11-03"）
 * @property open 始値
 * @property high 高値
 * @property low 安値
 * @property close 終値
 * @property volume 出来高
 */
data class CandleItem(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
