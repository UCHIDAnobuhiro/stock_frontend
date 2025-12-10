package com.example.stock.di

import com.example.stock.core.util.DefaultDispatcherProvider
import com.example.stock.core.util.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing coroutine dispatcher dependencies.
 *
 * This module binds the [DispatcherProvider] interface to its default implementation,
 * allowing for easy testing by swapping out the implementation in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {

    /**
     * Binds [DefaultDispatcherProvider] as the implementation of [DispatcherProvider].
     */
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        defaultDispatcherProvider: DefaultDispatcherProvider
    ): DispatcherProvider
}
