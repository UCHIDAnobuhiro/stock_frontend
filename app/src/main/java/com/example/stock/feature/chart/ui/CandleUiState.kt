package com.example.stock.feature.chart.ui

import androidx.annotation.StringRes

/**
 * ローソク足チャート画面のUI状態。
 *
 * 読み込み状態、取得したローソク足データ、エラー情報を含む
 * チャート表示の現在の状態を表す。
 *
 * @property isLoading ローソク足データを読み込み中かどうか
 * @property items チャート表示用のローソク足アイテムリスト
 * @property errorResId エラーメッセージの文字列リソースID。エラーがない場合はnull
 */
data class CandleUiState(
    val isLoading: Boolean = false,
    val items: List<CandleItem> = emptyList(),
    @get:StringRes val errorResId: Int? = null
)
