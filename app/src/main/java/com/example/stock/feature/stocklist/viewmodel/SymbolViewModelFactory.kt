package com.example.stock.feature.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.feature.stocklist.data.StockRepository

/**
 * SymbolViewModelのインスタンス生成用Factoryクラス。
 * ViewModelProviderに渡して利用することで、依存性（StockRepository）を注入できる。
 *
 * @property repo StockRepositoryのインスタンス
 */
class SymbolViewModelFactory(
    private val repo: StockRepository
) : ViewModelProvider.Factory {
    /**
     * ViewModelのインスタンスを生成する。
     *
     * @param modelClass 生成するViewModelのクラス
     * @return SymbolViewModelのインスタンス
     * @throws IllegalArgumentException 未対応のViewModelクラスの場合
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SymbolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SymbolViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}