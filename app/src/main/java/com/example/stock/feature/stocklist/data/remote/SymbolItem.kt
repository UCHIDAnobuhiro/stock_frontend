package com.example.stock.feature.stocklist.data.remote

import kotlinx.serialization.Serializable

/**
 * Data class representing symbol information.
 *
 * @property code Symbol code
 * @property name Symbol name
 */
@Serializable
data class SymbolItem(
    val code: String, // Symbol code (e.g., "7203")
    val name: String  // Symbol name (e.g., "Toyota Motor")
)
