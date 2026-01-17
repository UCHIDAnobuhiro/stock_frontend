package com.example.stock.feature.stocklist.ui

/**
 * Symbol data model for UI display.
 *
 * Instead of directly passing the DTO from the API response to the UI,
 * a lightweight data structure formatted for UI consumption is defined.
 *
 * @property code Symbol code (e.g., "7203")
 * @property name Symbol name (e.g., "Toyota Motor")
 */
data class SymbolItem(
    val code: String,
    val name: String
)
