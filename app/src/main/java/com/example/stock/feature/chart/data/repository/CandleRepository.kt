package com.example.stock.feature.chart.data.repository

import com.example.stock.feature.chart.data.remote.CandleDto
import com.example.stock.feature.chart.data.remote.ChartApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Repository responsible for fetching and managing candlestick data.
 *
 * - Fetches candlestick data via API and exposes it via StateFlow
 * - Data fetching is executed on the IO thread
 *
 * @property chartApi Chart data API
 * @property io Coroutine dispatcher for IO thread
 */
class CandleRepository(
    private val chartApi: ChartApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    // StateFlow for candlestick data (read-only)
    private val _candles = MutableStateFlow<List<CandleDto>>(emptyList())
    val candles: StateFlow<List<CandleDto>> = _candles

    /**
     * Fetches candlestick data for a specified symbol code, interval, and count, and updates StateFlow.
     *
     * @param code Symbol code
     * @param interval Data fetching interval (e.g., "1day")
     * @param outputsize Number of data points to fetch (default: 200)
     */
    suspend fun fetchCandles(
        code: String,
        interval: String = "1day",
        outputsize: Int = 200
    ) = withContext(io) {
        _candles.value = chartApi.getCandles(code, interval, outputsize)
    }

    /**
     * Clears the stored candlestick data.
     */
    fun clearCandles() {
        _candles.value = emptyList()
    }
}
