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
 * Application class for the Stock app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class StockApplication : Application() {

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
     * Restores the JWT token from persistent storage to memory.
     * This ensures the user remains logged in after app restart.
     */
    private fun restoreToken() {
        applicationScope.launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                this@StockApplication,
                TokenEntryPoint::class.java
            )
            entryPoint.tokenStore().tokenFlow.first()?.let { token ->
                entryPoint.tokenProvider().update(token)
                Timber.d("Token restored from storage")
            }
        }
    }
}
