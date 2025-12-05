package com.example.stock.feature.stocklist.ui

import com.example.stock.feature.stocklist.data.remote.SymbolItem

data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolItem> = emptyList(),
    val error: String? = null
)
