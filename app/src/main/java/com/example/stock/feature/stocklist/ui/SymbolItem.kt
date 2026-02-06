package com.example.stock.feature.stocklist.ui

/**
 * UI表示用の銘柄データモデル。
 *
 * APIレスポンスのDTOを直接UIに渡すのではなく、
 * UI向けにフォーマットされた軽量なデータ構造を定義する。
 *
 * @property code 銘柄コード（例："7203"）
 * @property name 銘柄名（例："トヨタ自動車"）
 */
data class SymbolItem(
    val code: String,
    val name: String
)
