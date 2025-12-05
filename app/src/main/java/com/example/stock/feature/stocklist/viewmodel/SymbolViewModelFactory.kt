package com.example.stock.feature.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.feature.stocklist.data.repository.StockRepository

/**
 * Factory class for creating instances of SymbolViewModel.
 * By passing it to ViewModelProvider, dependencies (StockRepository) can be injected.
 *
 * @property repo Instance of StockRepository
 */
class SymbolViewModelFactory(
    private val repo: StockRepository
) : ViewModelProvider.Factory {
    /**
     * Creates an instance of ViewModel.
     *
     * @param modelClass Class of the ViewModel to create
     * @return Instance of SymbolViewModel
     * @throws IllegalArgumentException If the ViewModel class is not supported
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SymbolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SymbolViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}