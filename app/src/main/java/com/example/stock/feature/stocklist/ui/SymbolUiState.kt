package com.example.stock.feature.stocklist.ui

import androidx.annotation.StringRes
import com.example.stock.feature.stocklist.data.remote.SymbolDto

/**
 * UI state for the symbol list screen.
 *
 * Represents the current state of the symbol list display,
 * including loading status, fetched symbols, and error information.
 *
 * @property isLoading Whether the symbol list is currently being loaded
 * @property symbols List of symbols fetched from the API
 * @property errorResId String resource ID for error message, or null if no error
 */
data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolDto> = emptyList(),
    @StringRes val errorResId: Int? = null
)
