package com.example.stock.feature.stocklist.data.remote

import retrofit2.http.GET

/**
 * Retrofit interface defining communication with the stock information API.
 *
 * Provides APIs for retrieving symbol lists.
 */
interface StockApi {
    /**
     * API for fetching the list of symbols.
     *
     * @return List of symbol information
     */
    @GET("symbols")
    suspend fun getSymbols(): List<SymbolItem>
}