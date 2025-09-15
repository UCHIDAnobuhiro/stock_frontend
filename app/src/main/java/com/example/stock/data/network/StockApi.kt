package com.example.stock.data.network

import com.example.stock.data.model.SymbolItem
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class CandleDto(
    val time: String,  // "YYYY-MM-DD"
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

interface StockApi {
    @GET("symbols")
    suspend fun getSymbols(): List<SymbolItem>

    @GET("candles/{code}")
    suspend fun getCandles(
        @Path("code") code: String,
        @Query("interval") interval: String = "1day",
        @Query("outputsize") outputsize: Int = 200
    ): List<CandleDto>
}