package com.example.stock.feature.chart.data.repository

import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.chart.data.remote.CandleDto
import com.example.stock.feature.chart.data.remote.ChartApi
import com.example.stock.feature.chart.domain.model.Candle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ローソク足データの取得と管理を担当するリポジトリ。
 *
 * - API経由でローソク足データを取得し、StateFlowで公開
 * - データ取得はIOスレッドで実行
 *
 * @property chartApi チャートデータAPI
 * @property dispatcherProvider コルーチンディスパッチャーのプロバイダー
 */
@Singleton
class CandleRepository @Inject constructor(
    private val chartApi: ChartApi,
    private val dispatcherProvider: DispatcherProvider
) {
    // ローソク足データ用のStateFlow（読み取り専用）
    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles

    /**
     * 指定された銘柄コード、間隔、件数のローソク足データを取得し、StateFlowを更新する。
     *
     * @param code 銘柄コード
     * @param interval データ取得間隔（例："1day"）
     * @param outputsize 取得するデータ件数（デフォルト：200）
     */
    suspend fun fetchCandles(
        code: String,
        interval: String = "1day",
        outputsize: Int = 200
    ) = withContext(dispatcherProvider.io) {
        _candles.value = chartApi.getCandles(code, interval, outputsize).map { it.toEntity() }
    }

    /**
     * 保存されているローソク足データをクリアする。
     */
    fun clearCandles() {
        _candles.value = emptyList()
    }
}

private fun CandleDto.toEntity() = Candle(
    time = time,
    open = open,
    high = high,
    low = low,
    close = close,
    volume = volume
)
