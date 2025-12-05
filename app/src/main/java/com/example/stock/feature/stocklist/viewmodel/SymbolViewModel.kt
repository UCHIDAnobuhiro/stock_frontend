package com.example.stock.feature.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.feature.stocklist.data.repository.StockRepository
import com.example.stock.feature.stocklist.ui.SymbolUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel that manages symbol list data.
 *
 * @property repo Repository for fetching symbol data
 */
class SymbolViewModel(private val repo: StockRepository) : ViewModel() {

    private val _ui = MutableStateFlow(SymbolUiState())
    val ui: StateFlow<SymbolUiState> = _ui

    /**
     * Loads the symbol list.
     *
     * - Sets isLoading to true at the start to notify loading state
     * - Fetches the symbol list from Repository and updates UI state on success
     * - Sets error message and reflects it in UI on failure
     *
     * Executed asynchronously in ViewModelScope, so it remains safe even if the screen is recreated due to rotation.
     */
    fun load() = viewModelScope.launch {
        _ui.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.fetchSymbols() }
            .onSuccess { list ->
                _ui.update { it.copy(symbols = list, isLoading = false) }
            }
            .onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Loading failed", isLoading = false) }
            }
    }
}