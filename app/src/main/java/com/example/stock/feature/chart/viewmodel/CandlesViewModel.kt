package com.example.stock.feature.chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.feature.chart.data.CandleUiState
import com.example.stock.feature.stocklist.data.remote.CandleDto
import com.example.stock.feature.stocklist.data.repository.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


/**
 * UI表示用のローソク足データモデル。
 *
 *  * APIレスポンスのDTOを直接UIに渡さず、UI用に整形された軽量データ構造を定義する。
 *  *
 *  * @property time 日時（例: "2025-11-03"）
 *  * @property open 始値
 *  * @property high 高値
 *  * @property low 安値
 *  * @property close 終値
 *  * @property volume 出来高
 */
data class CandleItem(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

/**
 * 株価のローソク足データを管理するViewModel。
 *
 * Repository からローソク足データを取得し、UIで利用しやすい状態として StateFlow で公開する。
 * また、読み込み中・エラー・データなどの UI 状態を一元的に保持する。
 *
 * @property repo 株価データ取得用のリポジトリ
 * @property io I/O 処理を行うためのコルーチンディスパッチャ
 */
class CandlesViewModel(
    private val repo: StockRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(CandleUiState())
    val ui: StateFlow<CandleUiState> = _ui


    /**
     * 現在実行中のデータ取得ジョブ。
     */
    private var loadJob: Job? = null

    /**
     * 指定した銘柄コード・間隔・取得件数でローソク足データを取得する。
     *
     * 処理中は UI を読み込み状態に更新し、成功時は CandleDto を CandleItem に変換して反映する。
     * エラー発生時はエラーメッセージを UI に通知する。
     *
     * @param code 銘柄コード
     * @param interval データ取得間隔（例: "1day"）
     * @param outputsize 取得件数（デフォルト: 200）
     */
    fun load(code: String, interval: String = "1day", outputsize: Int = 200) {
        if (code.isBlank()) {
            _ui.update { it.copy(error = "銘柄コードが空です") }
            return
        }
        // 前回実行中のジョブをキャンセルし、最新のリクエストのみを有効にする
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }

            runCatching {
                repo.fetchCandles(code, interval, outputsize)
            }.onSuccess {
                repo.candles.firstOrNull().orEmpty()
                    .map { dto -> dto.toUi() }
                    .let { list ->
                        _ui.update { it.copy(isLoading = false, items = list, error = null) }
                    }
            }.onFailure { e ->
                val msg = when (e) {
                    is IOException -> "通信エラー。ネットワークを確認してください。"
                    is HttpException -> "サーバーエラー: ${e.code()}"
                    else -> "不明なエラー: ${e.message}"
                }
                _ui.update { it.copy(isLoading = false, error = msg) }
            }
        }
    }

    /**
     * 保持しているローソク足データをクリアする。
     *
     * 実行中のジョブをキャンセルし、リポジトリと UI 状態を初期化する。
     */
    fun clear() {
        loadJob?.cancel()
        repo.clearCandles()
        _ui.value = CandleUiState()
    }

    /**
     * DTO を UI 表示用モデルに変換する。
     *
     * @receiver CandleDto API レスポンスモデル
     * @return CandleItem UI 用の軽量モデル
     */
    private fun CandleDto.toUi() = CandleItem(
        time = time, open = open, high = high, low = low, close = close, volume = volume
    )
}