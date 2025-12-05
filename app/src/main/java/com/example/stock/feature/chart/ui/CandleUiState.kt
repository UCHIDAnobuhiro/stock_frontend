package com.example.stock.feature.chart.ui

import com.example.stock.feature.chart.viewmodel.CandleItem

data class CandleUiState(
    val isLoading: Boolean = false,
    val items: List<CandleItem> = emptyList(),
    val error: String? = null
)
