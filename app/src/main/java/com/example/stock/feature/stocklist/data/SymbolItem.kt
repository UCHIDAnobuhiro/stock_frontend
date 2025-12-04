package com.example.stock.feature.stocklist.data

import kotlinx.serialization.Serializable

/**
 * 銘柄情報を表すデータクラス。
 *
 * @property code 銘柄コード
 * @property name 銘柄名
 */
@Serializable
data class SymbolItem(
    val code: String, // 銘柄コード（例: "7203"）
    val name: String  // 銘柄名（例: "トヨタ自動車"）
)
