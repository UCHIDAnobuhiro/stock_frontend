package com.example.stock.feature.stocklist.data.remote

import kotlinx.serialization.Serializable

/**
 * APIからの銘柄情報を表すデータ転送オブジェクト。
 *
 * @property code 銘柄コード（例："7203"）
 * @property name 銘柄名（例："トヨタ自動車"）
 */
@Serializable
data class SymbolDto(
    val code: String,
    val name: String
)
