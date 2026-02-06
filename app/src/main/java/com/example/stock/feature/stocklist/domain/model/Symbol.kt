package com.example.stock.feature.stocklist.domain.model

/**
 * Domain entity representing a stock symbol.
 *
 * @property code Symbol code (e.g., "AAPL", "GOOG")
 * @property name Symbol name (e.g., "Apple Inc.", "Alphabet Inc.")
 */
data class Symbol(
    val code: String,
    val name: String
)
