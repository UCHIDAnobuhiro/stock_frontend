package com.example.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.data.network.CandleDto
import com.example.stock.data.repository.StockRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 株価のローソク足データを管理するViewModel。
 *
 * @property repo 株価データ取得用のリポジトリ
 */
class CandlesViewModel(
    private val repo: StockRepository
) : ViewModel() {
    /**
     * 現在のローソク足データのリスト
     */
    val candles: StateFlow<List<CandleDto>> = repo.candles

    /**
     * 指定した銘柄コード・間隔・取得件数でローソク足データを取得する。
     *
     * @param code 銘柄コード
     * @param interval データ取得間隔（例: "1day"）
     * @param outputsize 取得件数（デフォルト: 200）
     */
    fun load(code: String, interval: String = "1day", outputsize: Int = 200) =
        viewModelScope.launch { repo.fetchCandles(code, interval, outputsize) }

    /**
    保持しているローソク足データをクリアする。
     */
    fun clear() = repo.clearCandles()
}