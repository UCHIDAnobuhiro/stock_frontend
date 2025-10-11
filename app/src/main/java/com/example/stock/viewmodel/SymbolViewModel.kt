package com.example.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.data.model.SymbolItem
import com.example.stock.data.repository.StockRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 銘柄リストのデータを管理するViewModel。
 *
 * @property repo 銘柄データ取得用のリポジトリ
 */
class SymbolViewModel(private val repo: StockRepository) : ViewModel() {
    /**
     * 現在の銘柄リスト
     */
    val symbols: StateFlow<List<SymbolItem>> = repo.symbols

    /**
     * 銘柄リストをリポジトリから取得する。
     */
    fun load() = viewModelScope.launch {
        repo.fetchSymbols()
    }
}