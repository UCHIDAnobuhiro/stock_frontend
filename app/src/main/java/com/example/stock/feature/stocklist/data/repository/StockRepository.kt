package com.example.stock.feature.stocklist.data.repository

import com.example.stock.core.network.ApiClient
import com.example.stock.feature.stocklist.data.remote.StockApi
import com.example.stock.feature.stocklist.data.remote.SymbolItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Repository responsible for fetching and managing stock information.
 *
 * - Fetches symbol lists and candlestick data via API and exposes them via StateFlow
 * - Data fetching is executed on the IO thread
 *
 * @property stockApi Stock information API
 * @property io Coroutine dispatcher for IO thread
 */
class StockRepository(
    private val stockApi: StockApi = ApiClient.stockApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    // StateFlow for symbol list (read-only)
    private val _symbols = MutableStateFlow<List<SymbolItem>>(emptyList())
    val symbols: StateFlow<List<SymbolItem>> = _symbols

    /**
     * Fetches the symbol list from the API.
     *
     * - Network communication is executed on the IO thread
     * - Data is not reflected in StateFlow but returned as a List to the caller (e.g., ViewModel)
     *
     * @return Symbol list fetched from the API
     */
    suspend fun fetchSymbols(): List<SymbolItem> = withContext(io) {
        stockApi.getSymbols()
    }
}