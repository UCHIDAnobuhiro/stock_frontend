package com.example.stock.feature.chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.chart.data.repository.CandleRepository
import com.example.stock.feature.chart.domain.model.Candle
import com.example.stock.feature.chart.ui.CandleItem
import com.example.stock.feature.chart.ui.CandleUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * 株価ローソク足データを管理するViewModel。
 *
 * Repositoryからローソク足データを取得し、UIで使いやすい状態で
 * StateFlow経由で公開する。
 * さらに、読み込み、エラー、データなどのUI状態を一元管理する。
 *
 * @property repo ローソク足データ取得用のリポジトリ
 * @property dispatcherProvider コルーチンディスパッチャーのプロバイダー。テスト容易性を実現。
 */
@HiltViewModel
class CandlesViewModel @Inject constructor(
    private val repo: CandleRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(CandleUiState())
    val ui: StateFlow<CandleUiState> = _ui


    /**
     * 現在実行中のデータ取得ジョブ。
     */
    private var loadJob: Job? = null

    /**
     * 指定された銘柄コード、間隔、件数のローソク足データを取得する。
     *
     * 処理中はUIを読み込み状態に更新し、成功時は
     * CandleをCandleItemに変換して適用する。
     * エラー発生時はUIにエラーメッセージを通知する。
     *
     * @param code 銘柄コード
     * @param interval データ取得間隔（例："1day"）
     * @param outputsize 取得するレコード数（デフォルト：200）
     */
    fun load(code: String, interval: String = "1day", outputsize: Int = 200) {
        if (code.isBlank()) {
            _ui.update { it.copy(errorResId = R.string.error_empty_stock_code) }
            return
        }
        // 前のジョブをキャンセルし、最新のリクエストのみをアクティブに保つ
        loadJob?.cancel()
        loadJob = viewModelScope.launch(dispatcherProvider.main) {
            _ui.update { it.copy(isLoading = true, errorResId = null) }

            runCatching {
                withContext(dispatcherProvider.io) {
                    repo.fetchCandles(code, interval, outputsize)
                }
            }.onSuccess {
                repo.candles.firstOrNull().orEmpty()
                    .map { entity -> entity.toUi() }
                    .let { list ->
                        _ui.update { it.copy(isLoading = false, items = list, errorResId = null) }
                    }
            }.onFailure { e ->
                // キャンセルのセマンティクスを維持するためCancellationExceptionを再スロー
                if (e is CancellationException) throw e
                val errorResId = when (e) {
                    is IOException -> R.string.error_network
                    is HttpException -> R.string.error_server
                    is SerializationException -> R.string.error_json
                    else -> R.string.error_unknown
                }
                _ui.update { it.copy(isLoading = false, errorResId = errorResId) }
            }
        }
    }

    /**
     * 保持しているローソク足データをクリアする。
     *
     * 実行中のジョブをキャンセルし、リポジトリとUI状態を初期化する。
     */
    fun clear() {
        loadJob?.cancel()
        repo.clearCandles()
        _ui.value = CandleUiState()
    }

    /**
     * ドメインエンティティをUI表示用モデルに変換する。
     *
     * @receiver Candle ドメインエンティティ
     * @return CandleItem UI用の軽量モデル
     */
    private fun Candle.toUi() = CandleItem(
        time = time, open = open, high = high, low = low, close = close, volume = volume
    )
}