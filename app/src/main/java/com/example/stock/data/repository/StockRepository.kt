package com.example.stock.data.repository

import com.example.stock.data.model.SymbolItem
import com.example.stock.data.network.ApiClient
import com.example.stock.data.network.CandleDto
import com.example.stock.data.network.StockApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * 株価情報の取得・管理を担当するリポジトリ。
 *
 * - 銘柄リストやローソク足データをAPI経由で取得し、StateFlowで公開する
 * - データ取得はIOスレッドで実行される
 *
 * @property stockApi 株価情報API
 * @property io IOスレッド用ディスパッチャ
 */
class StockRepository(
    private val stockApi: StockApi = ApiClient.stockApi,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    // 銘柄一覧のStateFlow（読み取り専用）
    private val _symbols = MutableStateFlow<List<SymbolItem>>(emptyList())
    val symbols: StateFlow<List<SymbolItem>> = _symbols

    // ローソク足データのStateFlow（読み取り専用）
    private val _candles = MutableStateFlow<List<CandleDto>>(emptyList())
    val candles: StateFlow<List<CandleDto>> = _candles

    /**
     * 銘柄リストを API から取得する。
     *
     * - ネットワーク通信は IO スレッドで実行される
     * - データは StateFlow には反映せず、呼び出し元（ViewModel など）に List として返す
     *
     * @return API から取得した銘柄リスト
     */
    suspend fun fetchSymbols(): List<SymbolItem> = withContext(io) {
        stockApi.getSymbols()
    }

    /**
     * 指定した銘柄コード・間隔・件数でローソク足データを取得し、StateFlowに反映する。
     *
     * @param code 銘柄コード
     * @param interval データ取得間隔（例: "1day"）
     * @param outputsize 取得件数（デフォルト: 200）
     */
    suspend fun fetchCandles(
        code: String,
        interval: String = "1day",
        outputsize: Int = 200
    ) = withContext(io) {
        _candles.value = stockApi.getCandles(code, interval, outputsize)
    }

    /**
     * 保持しているローソク足データをクリアする。
     */
    fun clearCandles() {
        _candles.value = emptyList()
    }
}