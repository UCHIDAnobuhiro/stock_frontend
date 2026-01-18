package com.example.stock.feature.chart.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Data Transfer Object representing stock candlestick data.
 *
 * @property time Date in "YYYY-MM-DD" format
 * @property open Opening price
 * @property high High price
 * @property low Low price
 * @property close Closing price
 * @property volume Trading volume
 */
@Serializable
data class CandleDto(
    val time: String,  // "YYYY-MM-DD"
    val open: Double,  // Opening price
    val high: Double,  // High price
    val low: Double,   // Low price
    val close: Double, // Closing price
    val volume: Long   // Trading volume
)

/**
 * Retrofit interface defining communication with the chart data API.
 *
 * Provides APIs for retrieving candlestick data.
 */
interface ChartApi {
    /**
     * API for fetching candlestick data for a specified symbol code.
     *
     * @param code Symbol code
     * @param interval Data fetching interval (e.g., "1day")
     * @param outputsize Number of data points to fetch (default: 200)
     * @return List of candlestick data
     */
    @GET("v1/candles/{code}")
    suspend fun getCandles(
        @Path("code") code: String,
        @Query("interval") interval: String = "1day",
        @Query("outputsize") outputsize: Int = 200
    ): List<CandleDto>
}
