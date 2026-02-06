package com.example.stock.feature.chart.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 株価ローソク足データを表すデータ転送オブジェクト。
 *
 * @property time "YYYY-MM-DD"形式の日付
 * @property open 始値
 * @property high 高値
 * @property low 安値
 * @property close 終値
 * @property volume 出来高
 */
@Serializable
data class CandleDto(
    val time: String,  // "YYYY-MM-DD"
    val open: Double,  // 始値
    val high: Double,  // 高値
    val low: Double,   // 安値
    val close: Double, // 終値
    val volume: Long   // 出来高
)

/**
 * チャートデータAPIとの通信を定義するRetrofitインターフェース。
 *
 * ローソク足データ取得用のAPIを提供する。
 */
interface ChartApi {
    /**
     * 指定された銘柄コードのローソク足データを取得するAPI。
     *
     * @param code 銘柄コード
     * @param interval データ取得間隔（例："1day"）
     * @param outputsize 取得するデータ件数（デフォルト：200）
     * @return ローソク足データのリスト
     */
    @GET("v1/candles/{code}")
    suspend fun getCandles(
        @Path("code") code: String,
        @Query("interval") interval: String = "1day",
        @Query("outputsize") outputsize: Int = 200
    ): List<CandleDto>
}
