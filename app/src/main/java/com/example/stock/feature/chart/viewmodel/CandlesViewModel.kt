package com.example.stock.feature.chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.chart.data.repository.CandleRepository
import com.example.stock.feature.chart.domain.model.Candle
import com.example.stock.feature.chart.ui.CandleItem
import com.example.stock.feature.chart.ui.CandleUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel that manages candlestick data for stock prices.
 *
 * Fetches candlestick data from the Repository and exposes it via StateFlow
 * in a state that is easy to use in the UI.
 * Additionally, centrally manages UI state such as loading, error, and data.
 *
 * @property repo Repository for fetching candlestick data
 * @property dispatcherProvider Provider for coroutine dispatchers, enabling testability
 */
@HiltViewModel
class CandlesViewModel @Inject constructor(
    private val repo: CandleRepository,
    private val dispatcherProvider: DispatcherProvider
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
     * converts Candle to CandleItem and applies it.
     * When an error occurs, notifies the UI with an error message.
     *
     * @param code Stock code
     * @param interval Data fetch interval (e.g., "1day")
     * @param outputsize Number of records to fetch (default: 200)
     */
    fun load(code: String, interval: String = "1day", outputsize: Int = 200) {
        if (code.isBlank()) {
            _ui.update { it.copy(errorResId = R.string.error_empty_stock_code) }
            return
        }
        // Cancel the previous job and keep only the latest request active
        loadJob?.cancel()
        loadJob = viewModelScope.launch(dispatcherProvider.main) {
            _ui.update { it.copy(isLoading = true, errorResId = null) }

            runCatching {
                withContext(dispatcherProvider.io) {
                    repo.fetchCandles(code, interval, outputsize)
                }
            }.onSuccess {
                repo.candles.firstOrNull().orEmpty()
                    .map { entity -> entity.toUi() }
                    .let { list ->
                        _ui.update { it.copy(isLoading = false, items = list, errorResId = null) }
                    }
            }.onFailure { e ->
                // Re-throw CancellationException to preserve cancellation semantics
                if (e is CancellationException) throw e
                val errorResId = when (e) {
                    is IOException -> R.string.error_network
                    is HttpException -> R.string.error_server
                    is SerializationException -> R.string.error_json
                    else -> R.string.error_unknown
                }
                _ui.update { it.copy(isLoading = false, errorResId = errorResId) }
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
     * Converts domain entity to UI display model.
     *
     * @receiver Candle Domain entity
     * @return CandleItem Lightweight model for UI
     */
    private fun Candle.toUi() = CandleItem(
        time = time, open = open, high = high, low = low, close = close, volume = volume
    )
}