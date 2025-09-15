package com.example.stock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.data.repository.StockRepository
import com.example.stock.viewmodel.CandlesViewModel

class CandlesViewModelFactory(
    private val repo: StockRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CandlesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CandlesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}