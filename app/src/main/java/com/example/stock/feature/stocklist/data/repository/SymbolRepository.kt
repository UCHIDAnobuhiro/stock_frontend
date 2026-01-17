package com.example.stock.feature.stocklist.data.repository

import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for fetching symbol information.
 *
 * - Fetches symbol lists via API
 * - Data fetching is executed on the IO thread
 *
 * @property symbolApi Symbol information API
 * @property io Coroutine dispatcher for IO thread
 */
class SymbolRepository(
    private val symbolApi: SymbolApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Fetches the symbol list from the API.
     *
     * - Network communication is executed on the IO thread
     *
     * @return Symbol list fetched from the API
     */
    suspend fun fetchSymbols(): List<SymbolDto> = withContext(io) {
        symbolApi.getSymbols()
    }
}
