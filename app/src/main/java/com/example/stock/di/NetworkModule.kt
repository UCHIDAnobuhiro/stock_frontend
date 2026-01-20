package com.example.stock.di

import com.example.stock.config.ApiConfig
import com.example.stock.core.data.auth.AuthEventManager
import com.example.stock.core.data.auth.InMemoryTokenProvider
import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.core.network.AuthInterceptor
import com.example.stock.core.network.TokenAuthenticator
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.chart.data.remote.ChartApi
import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module for providing network-related dependencies.
 * Installed in SingletonComponent to provide app-wide singleton instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a singleton instance of TokenProvider.
     */
    @Provides
    @Singleton
    fun provideTokenProvider(): TokenProvider {
        return InMemoryTokenProvider()
    }

    /**
     * Provides a singleton instance of HttpLoggingInterceptor.
     * Only logs request/response body in debug builds to prevent token leakage.
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Provides a singleton instance of OkHttpClient.
     *
     * @param tokenProvider Provider for authentication tokens
     * @param tokenStore Persistent storage for tokens
     * @param authEventManager Manager for authentication lifecycle events
     * @param loggingInterceptor Interceptor for HTTP request/response logging
     * @return Configured OkHttpClient with authentication and logging
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenProvider: TokenProvider,
        tokenStore: TokenStore,
        authEventManager: AuthEventManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(tokenProvider, tokenStore, authEventManager))
            .build()
    }

    /**
     * Provides a singleton instance of Json configuration.
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
        }
    }

    /**
     * Provides a singleton instance of Retrofit.
     *
     * @param okHttpClient HTTP client for network requests
     * @param json JSON serializer/deserializer configuration
     * @return Configured Retrofit instance for API calls
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Provides a singleton instance of AuthApi.
     *
     * @param retrofit Retrofit instance for creating API implementation
     * @return AuthApi implementation for authentication endpoints
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    /**
     * Provides a singleton instance of SymbolApi.
     *
     * @param retrofit Retrofit instance for creating API implementation
     * @return SymbolApi implementation for stock symbol endpoints
     */
    @Provides
    @Singleton
    fun provideSymbolApi(retrofit: Retrofit): SymbolApi {
        return retrofit.create(SymbolApi::class.java)
    }

    /**
     * Provides a singleton instance of ChartApi.
     *
     * @param retrofit Retrofit instance for creating API implementation
     * @return ChartApi implementation for candlestick chart data endpoints
     */
    @Provides
    @Singleton
    fun provideChartApi(retrofit: Retrofit): ChartApi {
        return retrofit.create(ChartApi::class.java)
    }
}
