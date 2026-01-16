package com.example.stock.feature.stocklist.data.remote

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object representing symbol information from the API.
 *
 * @property code Symbol code (e.g., "7203")
 * @property name Symbol name (e.g., "Toyota Motor")
 */
@Serializable
data class SymbolDto(
    val code: String,
    val name: String
)
