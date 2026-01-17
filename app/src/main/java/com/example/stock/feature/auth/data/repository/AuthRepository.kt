package com.example.stock.feature.auth.data.repository

import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.auth.data.remote.LoginRequest
import com.example.stock.feature.auth.data.remote.SignupRequest
import com.example.stock.feature.auth.data.remote.SignupResponse
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for authentication and token management.
 *
 * - Calls API during login and saves tokens to memory and persistent storage
 * - Clears tokens during logout
 *
 * @property api Authentication API
 * @property tokenStore Persistent token storage
 * @property tokenProvider In-memory token manager
 * @property dispatcherProvider Provider for coroutine dispatchers
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val tokenProvider: TokenProvider,
    private val dispatcherProvider: DispatcherProvider
) {
    /**
     * Handles login process.
     *
     * @param email Email address
     * @param password Password
     *
     * Authenticates via API and saves token to both memory and persistent storage.
     */
    suspend fun login(email: String, password: String) {
        val res = api.login(LoginRequest(email, password))
        // Update in-memory token (enables immediate Authorization header)
        tokenProvider.update(res.token)
        // Persist to storage (survives app restart)
        withContext(dispatcherProvider.io) {
            tokenStore.save(res.token)
        }
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
        withContext(dispatcherProvider.io) {
            tokenStore.clear()
        }
    }

    /**
     * Checks if a valid token exists in memory.
     *
     * @return true if a token exists, false otherwise
     */
    fun hasToken(): Boolean = tokenProvider.getToken() != null
}