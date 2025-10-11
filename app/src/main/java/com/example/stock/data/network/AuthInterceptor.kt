package com.example.stock.data.network

import com.example.stock.data.auth.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 認証トークンをリクエストヘッダーに付与するOkHttpインターセプター。
 *
 * TokenProviderから取得したトークンが存在する場合、
 * Authorizationヘッダー（Bearer認証）を自動的に追加する。
 * トークンが無い場合はヘッダーを追加しない。
 *
 * @property provider トークン取得用のTokenProvider
 */
class AuthInterceptor(private val provider: TokenProvider) : Interceptor {
    /**
     * リクエストにAuthorizationヘッダーを付与して次のチェーンへ進める。
     *
     * @param chain OkHttpのリクエストチェーン
     * @return レスポンス
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val t = provider.getToken() // トークンを取得
        val req = if (!t.isNullOrBlank()) {
            // トークンがあればAuthorizationヘッダーを追加
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $t")
                .build()
        } else chain.request() // トークンが無ければそのまま
        return chain.proceed(req)
    }
}