package com.example.stock.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.data.repository.StockRepository
import com.example.stock.viewmodel.CandlesViewModel

/**
 * CandlesViewModelのインスタンス生成用Factoryクラス。
 * ViewModelProviderに渡して利用することで、依存性（StockRepository）を注入できる。
 *
 * @property repo StockRepositoryのインスタンス
 */
class CandlesViewModelFactory(
    private val repo: StockRepository
) : ViewModelProvider.Factory {

    /**
     * ViewModelのインスタンスを生成する。
     *
     * @param modelClass 生成するViewModelのクラス
     * @return CandlesViewModelのインスタンス
     * @throws IllegalArgumentException 未対応のViewModelクラスの場合
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CandlesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CandlesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}