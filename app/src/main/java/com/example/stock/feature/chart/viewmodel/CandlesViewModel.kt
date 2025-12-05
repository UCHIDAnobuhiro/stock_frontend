package com.example.stock.feature.chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.feature.chart.ui.CandleUiState
import com.example.stock.feature.stocklist.data.remote.CandleDto
import com.example.stock.feature.stocklist.data.repository.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


/**
 * Candlestick data model for UI display.
 *
 *  * Instead of directly passing the DTO from the API response to the UI,
 *  * a lightweight data structure formatted for UI consumption is defined.
 *  *
 *  * @property time Date and time (e.g., "2025-11-03")
 *  * @property open Opening price
 *  * @property high High price
 *  * @property low Low price
 *  * @property close Closing price
 *  * @property volume Trading volume
 */
data class CandleItem(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

/**
 * ViewModel that manages candlestick data for stock prices.
 *
 * Fetches candlestick data from the Repository and exposes it via StateFlow
 * in a state that is easy to use in the UI.
 * Additionally, centrally manages UI state such as loading, error, and data.
 *
 * @property repo Repository for fetching stock price data
 * @property io Coroutine dispatcher for performing I/O operations
 */
class CandlesViewModel(
    private val repo: StockRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(CandleUiState())
    val ui: StateFlow<CandleUiState> = _ui


    /**
     * Currently executing data fetch job.
     */
    private var loadJob: Job? = null

    /**
     * Fetches candlestick data for the specified stock code, interval, and quantity.
     *
     * During processing, updates the UI to a loading state, and on success,
     * converts CandleDto to CandleItem and applies it.
     * When an error occurs, notifies the UI with an error message.
     *
     * @param code Stock code
     * @param interval Data fetch interval (e.g., "1day")
     * @param outputsize Number of records to fetch (default: 200)
     */
    fun load(code: String, interval: String = "1day", outputsize: Int = 200) {
        if (code.isBlank()) {
            _ui.update { it.copy(error = "Stock code is empty") }
            return
        }
        // Cancel the previous job and keep only the latest request active
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }

            runCatching {
                repo.fetchCandles(code, interval, outputsize)
            }.onSuccess {
                repo.candles.firstOrNull().orEmpty()
                    .map { dto -> dto.toUi() }
                    .let { list ->
                        _ui.update { it.copy(isLoading = false, items = list, error = null) }
                    }
            }.onFailure { e ->
                val msg = when (e) {
                    is IOException -> "Communication error. Please check your network."
                    is HttpException -> "Server error: ${e.code()}"
                    else -> "Unknown error: ${e.message}"
                }
                _ui.update { it.copy(isLoading = false, error = msg) }
            }
        }
    }

    /**
     * Clears the candlestick data being held.
     *
     * Cancels the executing job and initializes the repository and UI state.
     */
    fun clear() {
        loadJob?.cancel()
        repo.clearCandles()
        _ui.value = CandleUiState()
    }

    /**
     * Converts DTO to UI display model.
     *
     * @receiver CandleDto API response model
     * @return CandleItem Lightweight model for UI
     */
    private fun CandleDto.toUi() = CandleItem(
        time = time, open = open, high = high, low = low, close = close, volume = volume
    )
}