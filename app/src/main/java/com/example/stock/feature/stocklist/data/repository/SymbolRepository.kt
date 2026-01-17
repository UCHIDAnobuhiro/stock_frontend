package com.example.stock.feature.stocklist.data.repository

import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for fetching symbol information.
 *
 * - Fetches symbol lists via API
 * - Data fetching is executed on the IO thread
 *
 * @property symbolApi Symbol information API
 * @property dispatcherProvider Provider for coroutine dispatchers
 */
@Singleton
class SymbolRepository @Inject constructor(
    private val symbolApi: SymbolApi,
    private val dispatcherProvider: DispatcherProvider
) {
    /**
     * Fetches the symbol list from the API.
     *
     * - Network communication is executed on the IO thread
     *
     * @return Symbol list fetched from the API
     */
    suspend fun fetchSymbols(): List<SymbolDto> = withContext(dispatcherProvider.io) {
        symbolApi.getSymbols()
    }
}
