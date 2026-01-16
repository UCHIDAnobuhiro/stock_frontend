package com.example.stock.feature.stocklist.ui

import com.example.stock.feature.stocklist.data.remote.SymbolItem

/**
 * UI state for the stock list screen.
 *
 * Represents the current state of the symbol list display,
 * including loading status, fetched symbols, and error information.
 *
 * @property isLoading Whether the symbol list is currently being loaded
 * @property symbols List of stock symbols fetched from the API
 * @property error Error message to display, or null if no error
 */
data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolItem> = emptyList(),
    val error: String? = null
)
