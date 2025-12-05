package com.example.stock.core.network

import com.example.stock.core.data.auth.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that adds authentication tokens to request headers.
 *
 * If a token is obtained from TokenProvider, automatically adds an
 * Authorization header (Bearer authentication).
 * If no token exists, no header is added.
 *
 * @property provider TokenProvider for token retrieval
 */
class AuthInterceptor(private val provider: TokenProvider) : Interceptor {
    /**
     * Adds an Authorization header to the request and proceeds to the next chain.
     *
     * @param chain OkHttp request chain
     * @return Response
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val t = provider.getToken() // Retrieve token
        val req = if (!t.isNullOrBlank()) {
            // If token exists, add Authorization header
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $t")
                .build()
        } else chain.request() // If no token, use request as is
        return chain.proceed(req)
    }
}