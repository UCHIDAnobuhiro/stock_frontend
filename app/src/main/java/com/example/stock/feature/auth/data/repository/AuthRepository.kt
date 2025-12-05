package com.example.stock.feature.auth.data.repository

import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.auth.data.remote.LoginRequest
import com.example.stock.feature.auth.data.remote.LoginResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 認証・トークン管理を担当するリポジトリ。
 *
 * - ログイン時にAPIを呼び出し、トークンをメモリと永続ストアに保存
 * - ログアウト時にトークンをクリア
 *
 * @property api 認証API
 * @property tokenStore トークン永続化ストア
 * @property tokenProvider メモリ上のトークン管理
 * @property io IOスレッド用ディスパッチャ
 */
class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val tokenProvider: TokenProvider,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * ログイン処理。
     *
     * @param email メールアドレス
     * @param password パスワード
     * @return 認証レスポンス（トークン等）
     *
     * APIで認証し、トークンをメモリと永続ストアに保存する。
     */
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

    /**
     * ログアウト処理。
     *
     * メモリと永続ストア両方のトークンをクリアする。
     */
    suspend fun logout() {
        tokenProvider.clear()
        withContext(io) {
            tokenStore.clear()
        }
    }
}