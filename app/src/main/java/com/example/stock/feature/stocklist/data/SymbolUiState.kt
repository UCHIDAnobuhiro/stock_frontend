package com.example.stock.feature.stocklist.data

data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolItem> = emptyList(),
    val error: String? = null
)
