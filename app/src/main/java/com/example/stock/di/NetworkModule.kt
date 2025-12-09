package com.example.stock.di

import com.example.stock.config.ApiConfig
import com.example.stock.core.data.auth.InMemoryTokenProvider
import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.network.AuthInterceptor
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.stocklist.data.remote.StockApi
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
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Provides a singleton instance of OkHttpClient.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenProvider: TokenProvider,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
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
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    /**
     * Provides a singleton instance of StockApi.
     */
    @Provides
    @Singleton
    fun provideStockApi(retrofit: Retrofit): StockApi {
        return retrofit.create(StockApi::class.java)
    }
}
