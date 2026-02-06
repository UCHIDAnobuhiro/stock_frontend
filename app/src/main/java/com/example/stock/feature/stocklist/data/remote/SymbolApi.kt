package com.example.stock.feature.stocklist.data.remote

import retrofit2.http.GET

/**
 * 銘柄APIとの通信を定義するRetrofitインターフェース。
 *
 * 銘柄リスト取得用のAPIを提供する。
 */
interface SymbolApi {
    /**
     * 銘柄リストを取得するAPI。
     *
     * @return 銘柄情報のリスト
     */
    @GET("v1/symbols")
    suspend fun getSymbols(): List<SymbolDto>
}
