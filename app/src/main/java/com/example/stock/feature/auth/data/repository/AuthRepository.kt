package com.example.stock.feature.auth.data.repository

import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.auth.data.remote.LoginRequest
import com.example.stock.feature.auth.data.remote.LoginResponse
import com.example.stock.feature.auth.data.remote.SignupRequest
import com.example.stock.feature.auth.data.remote.SignupResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for authentication and token management.
 *
 * - Calls API during login and saves tokens to memory and persistent storage
 * - Clears tokens during logout
 *
 * @property api Authentication API
 * @property tokenStore Persistent token storage
 * @property tokenProvider In-memory token manager
 * @property io Dispatcher for IO operations
 */
class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val tokenProvider: TokenProvider,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Handles login process.
     *
     * @param email Email address
     * @param password Password
     * @return Authentication response containing token
     *
     * Authenticates via API and saves token to both memory and persistent storage.
     */
    suspend fun login(email: String, password: String): LoginResponse {
        val res = api.login(LoginRequest(email, password))
        // Update in-memory token (enables immediate Authorization header)
        tokenProvider.update(res.token)
        // Persist to storage (survives app restart)
        withContext(io) {
            tokenStore.save(res.token)
        }
        return res
    }

    /**
     * Handles signup process.
     *
     * @param email Email address
     * @param password Password
     * @return Authentication response containing message
     *
     * Registers new user via API.
     */
    suspend fun signup(email: String, password: String): SignupResponse {
        return api.signup(SignupRequest(email, password))
    }

    /**
     * Handles logout process.
     *
     * Clears tokens from both memory and persistent storage.
     */
    suspend fun logout() {
        tokenProvider.clear()
        withContext(io) {
            tokenStore.clear()
        }
    }
}