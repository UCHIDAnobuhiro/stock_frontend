package com.example.stock.feature.stocklist.data.remote

import retrofit2.http.GET

/**
 * Retrofit interface defining communication with the symbol API.
 *
 * Provides APIs for retrieving symbol lists.
 */
interface SymbolApi {
    /**
     * API for fetching the list of symbols.
     *
     * @return List of symbol information
     */
    @GET("v1/symbols")
    suspend fun getSymbols(): List<SymbolDto>
}
