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
 * 401 Unauthorizedレスポンスを処理するOkHttp Authenticator。
 *
 * 401レスポンスを受信した際：
 * 1. メモリと永続ストレージからトークンをクリア
 * 2. [AuthEventManager]経由でセッション期限切れイベントを発行
 * 3. リトライしないことを示すためにnullを返す（ユーザーは再ログインが必要）
 *
 * 将来的にはリフレッシュトークンのサポートに拡張可能：
 * - アクセストークンのリフレッシュを試行
 * - 成功した場合、新しいトークンで元のリクエストをリトライ
 * - リフレッシュ失敗時はログアウト処理を実行
 *
 * @property tokenProvider メモリ上のトークンマネージャー
 * @property tokenStore 永続トークンストレージ
 * @property authEventManager グローバル認証イベント発行者
 */
class TokenAuthenticator(
    private val tokenProvider: TokenProvider,
    private val tokenStore: TokenStore,
    private val authEventManager: AuthEventManager
) : Authenticator {

    /**
     * 401レスポンスを受信した際に呼び出される。
     *
     * @param route リクエストのルート（nullable）
     * @param response 401レスポンス
     * @return リトライしない場合はnull、更新された認証情報でリトライする場合は新しいRequest
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // 無限リトライループを防止 - 既に認証を試みている場合は諦める
        if (response.request.header("Authorization") == null) {
            return null
        }

        // メモリとストレージからトークンをクリア
        tokenProvider.clear()
        runBlocking {
            tokenStore.clear()
        }

        // ログイン画面への遷移をトリガーするためセッション期限切れイベントを発行
        authEventManager.emitSessionExpired()

        // リトライしないことを示すためにnullを返す - ユーザーは再ログインが必要
        return null
    }
}
