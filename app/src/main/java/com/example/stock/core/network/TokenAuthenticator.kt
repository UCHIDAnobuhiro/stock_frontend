package com.example.stock.core.network

import com.example.stock.core.data.auth.AuthEventManager
import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp Authenticator that handles 401 Unauthorized responses.
 *
 * When a 401 response is received:
 * 1. Clears the token from memory and persistent storage
 * 2. Emits a session expired event via [AuthEventManager]
 * 3. Returns null to indicate no retry (user must re-login)
 *
 * In the future, this can be extended to support refresh tokens:
 * - Attempt to refresh the access token
 * - If successful, retry the original request with the new token
 * - If refresh fails, proceed with logout
 *
 * @property tokenProvider In-memory token manager
 * @property tokenStore Persistent token storage
 * @property authEventManager Global auth event emitter
 */
class TokenAuthenticator(
    private val tokenProvider: TokenProvider,
    private val tokenStore: TokenStore,
    private val authEventManager: AuthEventManager
) : Authenticator {

    /**
     * Called when a 401 response is received.
     *
     * @param route The route of the request (nullable)
     * @param response The 401 response
     * @return null to indicate no retry, or a new Request to retry with updated credentials
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops - if we've already tried authenticating, give up
        if (response.request.header("Authorization") == null) {
            return null
        }

        // Clear tokens from memory and storage
        tokenProvider.clear()
        runBlocking {
            tokenStore.clear()
        }

        // Emit session expired event to trigger navigation to login
        authEventManager.emitSessionExpired()

        // Return null to indicate no retry - user must re-login
        return null
    }
}
