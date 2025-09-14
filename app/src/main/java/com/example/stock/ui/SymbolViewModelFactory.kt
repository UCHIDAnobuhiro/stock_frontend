package com.example.stock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.data.network.ApiClient
import com.example.stock.data.repository.StockRepository
import com.example.stock.viewmodel.SymbolViewModel

class SymbolViewModelFactory(
    private val repo: StockRepository = StockRepository(ApiClient.stockApi)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SymbolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SymbolViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}