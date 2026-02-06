package com.example.stock.feature.stocklist.domain.model

/**
 * 株式銘柄を表すドメインエンティティ。
 *
 * @property code 銘柄コード（例："AAPL", "GOOG"）
 * @property name 銘柄名（例："Apple Inc.", "Alphabet Inc."）
 */
data class Symbol(
    val code: String,
    val name: String
)
