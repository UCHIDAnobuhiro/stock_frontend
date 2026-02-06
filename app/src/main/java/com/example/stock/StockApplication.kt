package com.example.stock

import android.app.Application
import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Stockアプリケーションクラス。
 * @HiltAndroidAppアノテーションでHilt依存性注入を有効化。
 */
@HiltAndroidApp
class StockApplication : Application() {

    /**
     * 注入クラス外からトークン関連の依存関係にアクセスするためのHiltエントリーポイント。
     * アプリ起動時に永続ストレージからトークンを復元するために使用。
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TokenEntryPoint {
        fun tokenStore(): TokenStore
        fun tokenProvider(): TokenProvider
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        restoreToken()
    }

    /**
     * 永続ストレージからメモリにJWTトークンを復元する。
     * アプリ再起動後もユーザーがログイン状態を維持できるようにする。
     * 完了時に復元完了をマークし、LoginViewModelが安全に認証状態を
     * チェックできるようにする。
     */
    private fun restoreToken() {
        applicationScope.launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                this@StockApplication,
                TokenEntryPoint::class.java
            )
            try {
                entryPoint.tokenStore().tokenFlow.first()?.let { token ->
                    entryPoint.tokenProvider().update(token)
                    Timber.d("Token restored from storage")
                }
            } finally {
                entryPoint.tokenProvider().markRestorationComplete()
                Timber.d("Token restoration complete")
            }
        }
    }
}
