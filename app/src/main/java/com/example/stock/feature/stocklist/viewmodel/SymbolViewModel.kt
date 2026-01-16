package com.example.stock.feature.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.feature.stocklist.data.repository.SymbolRepository
import com.example.stock.feature.stocklist.ui.SymbolUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel that manages symbol list data.
 *
 * @property repo Repository for fetching symbol data
 */
@HiltViewModel
class SymbolViewModel @Inject constructor(
    private val repo: SymbolRepository
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
    fun load() = viewModelScope.launch {
        _ui.update { it.copy(isLoading = true, errorResId = null) }
        runCatching { repo.fetchSymbols() }
            .onSuccess { list ->
                _ui.update { it.copy(symbols = list, isLoading = false) }
            }
            .onFailure { e ->
                val errorResId = when (e) {
                    is IOException -> R.string.error_network
                    is HttpException -> R.string.error_server
                    else -> R.string.error_unknown
                }
                _ui.update { it.copy(errorResId = errorResId, isLoading = false) }
            }
    }
}