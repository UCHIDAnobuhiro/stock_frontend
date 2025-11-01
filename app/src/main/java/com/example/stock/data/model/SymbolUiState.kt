package com.example.stock.data.model

data class SymbolUiState(
    val isLoading: Boolean = false,
    val symbols: List<SymbolItem> = emptyList(),
    val error: String? = null
)
