package com.example.stock.feature.stocklist.data.repository

import com.example.stock.core.network.ApiClient
import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Repository responsible for fetching and managing symbol information.
 *
 * - Fetches symbol lists via API and exposes them via StateFlow
 * - Data fetching is executed on the IO thread
 *
 * @property symbolApi Symbol information API
 * @property io Coroutine dispatcher for IO thread
 */
class SymbolRepository(
    private val symbolApi: SymbolApi = ApiClient.symbolApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    // StateFlow for symbol list (read-only)
    private val _symbols = MutableStateFlow<List<SymbolDto>>(emptyList())
    val symbols: StateFlow<List<SymbolDto>> = _symbols

    /**
     * Fetches the symbol list from the API.
     *
     * - Network communication is executed on the IO thread
     * - Data is not reflected in StateFlow but returned as a List to the caller (e.g., ViewModel)
     *
     * @return Symbol list fetched from the API
     */
    suspend fun fetchSymbols(): List<SymbolDto> = withContext(io) {
        symbolApi.getSymbols()
    }
}
