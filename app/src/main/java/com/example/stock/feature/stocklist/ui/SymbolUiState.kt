package com.example.stock.feature.stocklist.ui

import androidx.annotation.StringRes

/**
 * 銘柄リスト画面のUI状態。
 *
 * 読み込み状態、取得した銘柄、エラー情報を含む
 * 銘柄リスト表示の現在の状態を表す。
 *
 * @property isLoading 銘柄リストを読み込み中かどうか
 * @property symbols UI表示用の銘柄アイテムリスト
 * @property errorResId エラーメッセージの文字列リソースID。エラーがない場合はnull
 */
data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolItem> = emptyList(),
    @get:StringRes val errorResId: Int? = null
)
