package com.example.stock.di

import android.content.Context
import com.example.stock.core.data.local.TokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * データ層の依存関係を提供するHiltモジュール。
 * アプリ全体でシングルトンインスタンスを提供するためSingletonComponentにインストール。
 *
 * 注意：AuthRepository、SymbolRepository、CandleRepositoryは@Inject constructorを使用し、
 * Hiltによって自動的に提供される。
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * TokenStoreのシングルトンインスタンスを提供する。
     *
     * @param context DataStore初期化用のアプリケーションコンテキスト
     * @return 永続トークンストレージ用のTokenStoreインスタンス
     */
    @Provides
    @Singleton
    fun provideTokenStore(
        @ApplicationContext context: Context
    ): TokenStore {
        return TokenStore(context)
    }
}
