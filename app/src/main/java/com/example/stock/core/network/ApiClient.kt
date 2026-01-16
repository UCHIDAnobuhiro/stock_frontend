package com.example.stock.core.network

import com.example.stock.config.ApiConfig
import com.example.stock.core.data.auth.InMemoryTokenProvider
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.chart.data.remote.ChartApi
import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Client object for API communication using Retrofit.
 *
 * - Uses Kotlinx Serialization to parse JSON
 * - BASE_URL is configured for localhost on the emulator (10.0.2.2)
 */
@OptIn(ExperimentalSerializationApi::class)
object ApiClient {

    // TokenProvider shared across the entire app
    val tokenProvider = InMemoryTokenProvider()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenProvider))
        .addInterceptor(logging)
        .build()


    /**
     * Json configuration:
     * - `ignoreUnknownKeys`: Ignores properties with unknown keys in JSON
     */
    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Retrofit instance:
     * - `baseUrl`: Base URL for API endpoints
     * - Adds Kotlinx Serialization converter
     */
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * AuthApi instance
     * Created using Retrofit
     */
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    val symbolApi: SymbolApi = retrofit.create(SymbolApi::class.java)

    val chartApi: ChartApi = retrofit.create(ChartApi::class.java)
}