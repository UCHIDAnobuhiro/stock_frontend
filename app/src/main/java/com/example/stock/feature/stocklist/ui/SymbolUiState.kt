package com.example.stock.feature.stocklist.ui

import androidx.annotation.StringRes

/**
 * UI state for the symbol list screen.
 *
 * Represents the current state of the symbol list display,
 * including loading status, fetched symbols, and error information.
 *
 * @property isLoading Whether the symbol list is currently being loaded
 * @property symbols List of symbol items for UI display
 * @property errorResId String resource ID for error message, or null if no error
 */
data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolItem> = emptyList(),
    @StringRes val errorResId: Int? = null
)
