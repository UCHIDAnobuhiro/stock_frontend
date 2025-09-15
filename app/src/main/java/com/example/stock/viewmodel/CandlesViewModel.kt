package com.example.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.data.network.CandleDto
import com.example.stock.data.repository.StockRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CandlesViewModel(
    private val repo: StockRepository
) : ViewModel() {
    val candles: StateFlow<List<CandleDto>> = repo.candles
    fun load(code: String, interval: String = "1day", outputsize: Int = 200) =
        viewModelScope.launch { repo.fetchCandles(code, interval, outputsize) }
    fun clear() = repo.clearCandles()
}