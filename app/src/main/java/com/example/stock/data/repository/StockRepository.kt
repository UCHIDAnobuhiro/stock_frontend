package com.example.stock.data.repository

import com.example.stock.data.model.SymbolItem
import com.example.stock.data.network.ApiClient
import com.example.stock.data.network.CandleDto
import com.example.stock.data.network.StockApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class StockRepository(
    private val stockApi: StockApi = ApiClient.stockApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    // 銘柄一覧
    private val _symbols = MutableStateFlow<List<SymbolItem>>(emptyList())
    val symbols: StateFlow<List<SymbolItem>> = _symbols

    // ロウソク足
    private val _candles = MutableStateFlow<List<CandleDto>>(emptyList())
    val candles: StateFlow<List<CandleDto>> = _candles

    suspend fun fetchSymbols() = withContext(io) {
        _symbols.value = stockApi.getSymbols()
    }

    suspend fun fetchCandles(
        code: String,
        interval: String = "1day",
        outputsize: Int = 200
    ) = withContext(io) {
        _candles.value = stockApi.getCandles(code, interval, outputsize)
    }

    fun clearCandles() { _candles.value = emptyList()}
}