package com.example.stock.feature.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import com.example.stock.feature.stocklist.data.repository.SymbolRepository
import com.example.stock.feature.stocklist.ui.SymbolItem
import com.example.stock.feature.stocklist.ui.SymbolUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel that manages symbol list data.
 *
 * @property repo Repository for fetching symbol data
 * @property dispatcherProvider Provider for coroutine dispatchers, enabling testability
 */
@HiltViewModel
class SymbolViewModel @Inject constructor(
    private val repo: SymbolRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(SymbolUiState())
    val ui: StateFlow<SymbolUiState> = _ui

    /**
     * Loads the symbol list.
     *
     * - Sets isLoading to true at the start to notify loading state
     * - Fetches the symbol list from Repository and updates UI state on success
     * - Sets appropriate error message resource ID on failure
     *
     * Executed asynchronously in ViewModelScope, so it remains safe even if the screen is recreated due to rotation.
     */
    fun load() = viewModelScope.launch(dispatcherProvider.main) {
        _ui.update { it.copy(isLoading = true, errorResId = null) }
        runCatching {
            withContext(dispatcherProvider.io) { repo.fetchSymbols() }
        }
            .onSuccess { list ->
                _ui.update { it.copy(symbols = list.map { dto -> dto.toUi() }, isLoading = false) }
            }
            .onFailure { e ->
                val errorResId = when (e) {
                    is IOException -> R.string.error_network
                    is HttpException -> R.string.error_server
                    is SerializationException -> R.string.error_json
                    else -> R.string.error_unknown
                }
                _ui.update { it.copy(errorResId = errorResId, isLoading = false) }
            }
    }

    /**
     * Converts DTO to UI display model.
     *
     * @receiver SymbolDto API response model
     * @return SymbolItem Lightweight model for UI
     */
    private fun SymbolDto.toUi() = SymbolItem(
        code = code,
        name = name
    )
}