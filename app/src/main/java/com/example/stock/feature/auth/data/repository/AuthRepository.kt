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
 * 認証とトークン管理を担当するリポジトリ。
 *
 * - ログイン時にAPIを呼び出し、トークンをメモリと永続ストレージに保存
 * - ログアウト時にトークンをクリア
 *
 * @property api 認証API
 * @property tokenStore 永続トークンストレージ
 * @property tokenProvider メモリ上のトークンマネージャー
 * @property dispatcherProvider コルーチンディスパッチャーのプロバイダー
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val tokenProvider: TokenProvider,
    private val dispatcherProvider: DispatcherProvider
) {
    /**
     * ログイン処理を実行する。
     *
     * @param email メールアドレス
     * @param password パスワード
     *
     * APIで認証し、トークンをメモリと永続ストレージの両方に保存する。
     */
    suspend fun login(email: String, password: String) {
        val res = api.login(LoginRequest(email, password))
        // メモリ上のトークンを更新（即座にAuthorizationヘッダーが有効になる）
        tokenProvider.update(res.token)
        // ストレージに永続化（アプリ再起動後も有効）
        withContext(dispatcherProvider.io) {
            tokenStore.save(res.token)
        }
    }

    /**
     * サインアップ処理を実行する。
     *
     * @param email メールアドレス
     * @param password パスワード
     * @return メッセージを含む認証レスポンス
     *
     * APIで新規ユーザーを登録する。
     */
    suspend fun signup(email: String, password: String): SignupResponse {
        return api.signup(SignupRequest(email, password))
    }

    /**
     * ログアウト処理を実行する。
     *
     * メモリと永続ストレージの両方からトークンをクリアする。
     */
    suspend fun logout() {
        tokenProvider.clear()
        withContext(dispatcherProvider.io) {
            tokenStore.clear()
        }
    }

    /**
     * メモリ上に有効なトークンが存在するかを確認する。
     * 確認前にストレージからのトークン復元完了を待機する。
     *
     * @return トークンが存在する場合はtrue、それ以外はfalse
     */
    suspend fun hasToken(): Boolean {
        tokenProvider.awaitRestoration()
        return tokenProvider.getToken() != null
    }
}