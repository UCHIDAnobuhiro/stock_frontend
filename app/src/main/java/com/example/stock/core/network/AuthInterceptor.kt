package com.example.stock.core.network

import com.example.stock.core.data.auth.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * リクエストヘッダーに認証トークンを付与するOkHttpインターセプター。
 *
 * TokenProviderからトークンを取得できた場合、自動的に
 * Authorizationヘッダー（Bearer認証）を付与する。
 * トークンが存在しない場合、ヘッダーは付与しない。
 *
 * @property provider トークン取得用のTokenProvider
 */
class AuthInterceptor(private val provider: TokenProvider) : Interceptor {
    /**
     * リクエストにAuthorizationヘッダーを付与し、次のチェーンに進める。
     *
     * @param chain OkHttpリクエストチェーン
     * @return レスポンス
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val t = provider.getToken() // トークンを取得
        val req = if (!t.isNullOrBlank()) {
            // トークンがあればAuthorizationヘッダーを付与
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $t")
                .build()
        } else chain.request() // トークンがなければリクエストをそのまま使用
        return chain.proceed(req)
    }
}