package com.example.stock.feature.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.core.network.ApiClient
import com.example.stock.feature.auth.data.repository.AuthRepository

/**
 * Factory class for creating LoginViewModel instances.
 * Enables dependency injection (AuthRepository) when passed to ViewModelProvider.
 *
 * @constructor
 * @param context Context used to access application context
 * @property appContext Application context
 */
class LoginViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    /**
     * Creates an instance of ViewModel.
     *
     * @param modelClass The class of ViewModel to create
     * @return LoginViewModel instance
     * @throws IllegalArgumentException If unsupported ViewModel class is provided
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = AuthRepository(
            api = ApiClient.authApi,
            tokenStore = TokenStore(appContext),
            tokenProvider = ApiClient.tokenProvider
        )
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(repo) as T
    }
}
