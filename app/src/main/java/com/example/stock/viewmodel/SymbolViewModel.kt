package com.example.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.data.model.SymbolItem
import com.example.stock.data.repository.StockRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SymbolViewModel (private val repo: StockRepository) : ViewModel() {
    val symbols: StateFlow<List<SymbolItem>> = repo.symbols
    fun load() = viewModelScope.launch {
        repo.fetchSymbols()
    }
}