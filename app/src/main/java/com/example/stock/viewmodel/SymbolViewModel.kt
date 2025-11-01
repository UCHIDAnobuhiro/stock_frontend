package com.example.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.data.model.SymbolUiState
import com.example.stock.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 銘柄リストのデータを管理するViewModel。
 *
 * @property repo 銘柄データ取得用のリポジトリ
 */
class SymbolViewModel(private val repo: StockRepository) : ViewModel() {

    private val _ui = MutableStateFlow(SymbolUiState())
    val ui: StateFlow<SymbolUiState> = _ui

    /**
     * 銘柄リストを読み込む。
     *
     * - 読み込み開始時に isLoading を true にしてローディング状態を通知
     * - Repository から銘柄リストを取得し、成功時に UI 状態を更新
     * - 失敗時はエラーメッセージを設定して UI に反映
     *
     * ViewModelScope で非同期実行されるため、画面回転などで再作成されても安全。
     */
    fun load() = viewModelScope.launch {
        _ui.update { it.copy(isLoading = true, error = null) }
        runCatching { repo.fetchSymbols() }
            .onSuccess { list ->
                _ui.update { it.copy(symbols = list, isLoading = false) }
            }
            .onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "読み込み失敗", isLoading = false) }
            }
    }
}