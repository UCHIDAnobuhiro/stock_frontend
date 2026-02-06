package com.example.stock.di

import com.example.stock.core.util.DefaultDispatcherProvider
import com.example.stock.core.util.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * コルーチンディスパッチャーの依存関係を提供するHiltモジュール。
 *
 * このモジュールは[DispatcherProvider]インターフェースをデフォルト実装にバインドし、
 * テスト時に実装を差し替えることでテストを容易にする。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {

    /**
     * [DefaultDispatcherProvider]を[DispatcherProvider]の実装としてバインドする。
     */
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        defaultDispatcherProvider: DefaultDispatcherProvider
    ): DispatcherProvider
}
