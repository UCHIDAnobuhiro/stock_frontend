package com.example.stock.feature.stocklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.stocklist.data.repository.SymbolRepository
import com.example.stock.feature.stocklist.domain.model.Symbol
import com.example.stock.feature.stocklist.ui.SymbolItem
import com.example.stock.feature.stocklist.ui.SymbolUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * 銘柄リストデータを管理するViewModel。
 *
 * @property repo 銘柄データ取得用のリポジトリ
 * @property dispatcherProvider コルーチンディスパッチャーのプロバイダー。テスト容易性を実現。
 */
@HiltViewModel
class SymbolViewModel @Inject constructor(
    private val repo: SymbolRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(SymbolUiState())
    val ui: StateFlow<SymbolUiState> = _ui

    /**
     * 銘柄リストを読み込む。
     *
     * - 開始時にisLoadingをtrueに設定し、読み込み状態を通知
     * - Repositoryから銘柄リストを取得し、成功時にUI状態を更新
     * - 失敗時に適切なエラーメッセージリソースIDを設定
     *
     * ViewModelScope内で非同期実行されるため、画面回転で再生成されても安全。
     */
    fun load() = viewModelScope.launch(dispatcherProvider.main) {
        _ui.update { it.copy(isLoading = true, errorResId = null) }
        runCatching {
            withContext(dispatcherProvider.io) { repo.fetchSymbols() }
        }
            .onSuccess { list ->
                _ui.update { it.copy(symbols = list.map { entity -> entity.toUi() }, isLoading = false) }
            }
            .onFailure { e ->
                val errorResId = when (e) {
                    is IOException -> R.string.error_network
                    is HttpException -> R.string.error_server
                    is SerializationException -> R.string.error_json
                    else -> R.string.error_unknown
                }
                _ui.update { it.copy(errorResId = errorResId, isLoading = false) }
            }
    }

    /**
     * ドメインエンティティをUI表示用モデルに変換する。
     *
     * @receiver Symbol ドメインエンティティ
     * @return SymbolItem UI用の軽量モデル
     */
    private fun Symbol.toUi() = SymbolItem(
        code = code,
        name = name
    )
}