package com.example.stock.ui.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.data.local.TokenStore
import com.example.stock.data.network.ApiClient
import com.example.stock.data.repository.AuthRepository
import com.example.stock.viewmodel.AuthViewModel

/**
 * AuthViewModelのインスタンス生成用Factoryクラス。
 * ViewModelProviderに渡して利用することで、依存性（AuthRepository）を注入できる。
 *
 * @constructor
 * @param context Context アプリケーションコンテキストを利用するための引数
 * @property appContext アプリケーションコンテキスト
 */
class AuthViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    /**
     * ViewModelのインスタンスを生成する。
     *
     * @param modelClass 生成するViewModelのクラス
     * @return AuthViewModelのインスタンス
     * @throws IllegalArgumentException 未対応のViewModelクラスの場合
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = AuthRepository(
            api = ApiClient.authApi,
            tokenStore = TokenStore(appContext),
            tokenProvider = ApiClient.tokenProvider
        )
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repo) as T
    }
}