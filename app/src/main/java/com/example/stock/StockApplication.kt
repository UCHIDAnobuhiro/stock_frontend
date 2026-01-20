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

    /**
     * Hilt entry point for accessing token-related dependencies outside of injected classes.
     * Used to restore tokens from persistent storage on app startup.
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
     * Restores the JWT token from persistent storage to memory.
     * This ensures the user remains logged in after app restart.
     * Marks restoration as complete when done, allowing LoginViewModel
     * to safely check auth state.
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
