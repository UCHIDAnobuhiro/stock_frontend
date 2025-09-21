package com.example.stock.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stock.data.local.TokenStore
import com.example.stock.data.network.ApiClient
import com.example.stock.data.repository.AuthRepository
import com.example.stock.viewmodel.AuthViewModel

class AuthViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

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