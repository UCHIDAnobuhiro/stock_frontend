package com.example.stock.data.model

import com.example.stock.viewmodel.CandleItem

data class CandleUiState(
    val isLoading: Boolean = false,
    val items: List<CandleItem> = emptyList(),
    val error: String? = null
)
