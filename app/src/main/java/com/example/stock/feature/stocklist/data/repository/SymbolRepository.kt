package com.example.stock.feature.stocklist.data.repository

import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import com.example.stock.feature.stocklist.domain.model.Symbol
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 銘柄情報の取得を担当するリポジトリ。
 *
 * - API経由で銘柄リストを取得
 * - データ取得はIOスレッドで実行
 *
 * @property symbolApi 銘柄情報API
 * @property dispatcherProvider コルーチンディスパッチャーのプロバイダー
 */
@Singleton
class SymbolRepository @Inject constructor(
    private val symbolApi: SymbolApi,
    private val dispatcherProvider: DispatcherProvider
) {
    /**
     * APIから銘柄リストを取得する。
     *
     * - ネットワーク通信はIOスレッドで実行
     *
     * @return APIから取得した銘柄リスト
     */
    suspend fun fetchSymbols(): List<Symbol> = withContext(dispatcherProvider.io) {
        symbolApi.getSymbols().map { it.toEntity() }
    }
}

private fun SymbolDto.toEntity() = Symbol(
    code = code,
    name = name
)
