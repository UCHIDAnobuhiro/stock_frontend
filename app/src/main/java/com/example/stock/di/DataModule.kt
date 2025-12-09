package com.example.stock.di

import android.content.Context
import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.auth.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing data layer dependencies.
 * Installed in SingletonComponent to provide app-wide singleton instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Provides a singleton instance of TokenStore.
     * Uses @ApplicationContext to inject application context.
     */
    @Provides
    @Singleton
    fun provideTokenStore(
        @ApplicationContext context: Context
    ): TokenStore {
        return TokenStore(context)
    }

    /**
     * Provides a singleton instance of AuthRepository.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApi,
        tokenStore: TokenStore,
        tokenProvider: TokenProvider
    ): AuthRepository {
        return AuthRepository(api, tokenStore, tokenProvider)
    }
}
