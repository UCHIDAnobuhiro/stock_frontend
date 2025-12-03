package com.example.stock.core.network

import com.example.stock.config.ApiConfig
import com.example.stock.core.data.auth.InMemoryTokenProvider
import com.example.stock.feature.auth.data.AuthApi
import com.example.stock.feature.stocklist.data.StockApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Retrofitを使ってAPI通信を行うためのクライアントオブジェクト。
 *
 * - Kotlinx Serializationを使用してJSONを解析
 * - BASE_URLはエミュレータ上のローカルホスト向けに設定（10.0.2.2）
 */
@OptIn(ExperimentalSerializationApi::class)
object ApiClient {

    // アプリ全体で共有する TokenProvider
    val tokenProvider = InMemoryTokenProvider()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenProvider))
        .addInterceptor(logging)
        .build()


    /**
     * Jsonの設定:
     * - `ignoreUnknownKeys`: キーが未知のJSONのプロパティを無視する
     */
    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Retrofitのインスタンス:
     * - `baseUrl`: APIエンドポイントのベースURL
     * - Kotlinx Serializationのコンバータを追加
     */
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * AuthApiのインスタンス
     * Retrofitを使用してインスタンスを作成
     */
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    val stockApi: StockApi = retrofit.create(StockApi::class.java)
}