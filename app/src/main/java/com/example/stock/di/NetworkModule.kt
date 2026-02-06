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
 * ネットワーク関連の依存関係を提供するHiltモジュール。
 * アプリ全体でシングルトンインスタンスを提供するためSingletonComponentにインストール。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * TokenProviderのシングルトンインスタンスを提供する。
     */
    @Provides
    @Singleton
    fun provideTokenProvider(): TokenProvider {
        return InMemoryTokenProvider()
    }

    /**
     * HttpLoggingInterceptorのシングルトンインスタンスを提供する。
     * トークン漏洩を防ぐため、デバッグビルドでのみリクエスト/レスポンスボディをログ出力する。
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
     * OkHttpClientのシングルトンインスタンスを提供する。
     *
     * @param tokenProvider 認証トークンのプロバイダー
     * @param tokenStore トークンの永続ストレージ
     * @param authEventManager 認証ライフサイクルイベントのマネージャー
     * @param loggingInterceptor HTTPリクエスト/レスポンスログ用インターセプター
     * @return 認証とログ機能を設定済みのOkHttpClient
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
     * Json設定のシングルトンインスタンスを提供する。
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
        }
    }

    /**
     * Retrofitのシングルトンインスタンスを提供する。
     *
     * @param okHttpClient ネットワークリクエスト用のHTTPクライアント
     * @param json JSONシリアライザー/デシリアライザー設定
     * @return API呼び出し用に設定済みのRetrofitインスタンス
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
     * AuthApiのシングルトンインスタンスを提供する。
     *
     * @param retrofit API実装を作成するためのRetrofitインスタンス
     * @return 認証エンドポイント用のAuthApi実装
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    /**
     * SymbolApiのシングルトンインスタンスを提供する。
     *
     * @param retrofit API実装を作成するためのRetrofitインスタンス
     * @return 銘柄エンドポイント用のSymbolApi実装
     */
    @Provides
    @Singleton
    fun provideSymbolApi(retrofit: Retrofit): SymbolApi {
        return retrofit.create(SymbolApi::class.java)
    }

    /**
     * ChartApiのシングルトンインスタンスを提供する。
     *
     * @param retrofit API実装を作成するためのRetrofitインスタンス
     * @return ローソク足チャートデータエンドポイント用のChartApi実装
     */
    @Provides
    @Singleton
    fun provideChartApi(retrofit: Retrofit): ChartApi {
        return retrofit.create(ChartApi::class.java)
    }
}
