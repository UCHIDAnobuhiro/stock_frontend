package com.example.stock.data.network

import com.example.stock.data.model.SymbolItem
import retrofit2.http.GET

interface StockApi {
    @GET("symbols")
    suspend fun getSymbols(): List<SymbolItem>
}