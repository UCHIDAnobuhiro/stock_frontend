package com.example.stock.data.repository

import com.example.stock.data.auth.TokenProvider
import com.example.stock.data.local.TokenStore
import com.example.stock.data.model.LoginRequest
import com.example.stock.data.model.LoginResponse
import com.example.stock.data.network.AuthApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val tokenProvider: TokenProvider,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun login(email: String, password: String): LoginResponse {
        val res = api.login(LoginRequest(email, password))
        // メモリに反映（即時に Authorization を付与できる）
        tokenProvider.update(res.token)
        // 永続化（再起動後も使える）
        withContext(io) {
            tokenStore.save(res.token)
        }
        return res
    }

    suspend fun logout() {
        tokenProvider.clear()
        withContext(io) {
            tokenStore.clear()
        }
    }
}