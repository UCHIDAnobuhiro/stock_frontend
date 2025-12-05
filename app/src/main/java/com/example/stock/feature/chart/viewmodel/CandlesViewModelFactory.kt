package com.example.stock.feature.chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.feature.stocklist.data.repository.StockRepository

/**
 * Factory class for generating CandlesViewModel instances.
 * By passing this to ViewModelProvider, dependencies (StockRepository) can be injected.
 *
 * @property repo Instance of StockRepository
 */
class CandlesViewModelFactory(
    private val repo: StockRepository
) : ViewModelProvider.Factory {

    /**
     * Generates a ViewModel instance.
     *
     * @param modelClass Class of the ViewModel to generate
     * @return Instance of CandlesViewModel
     * @throws IllegalArgumentException If an unsupported ViewModel class is specified
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CandlesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CandlesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}