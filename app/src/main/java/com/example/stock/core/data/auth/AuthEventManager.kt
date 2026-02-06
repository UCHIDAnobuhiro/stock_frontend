package com.example.stock.core.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * グローバル認証イベントマネージャー。
 *
 * トークン期限切れによる強制ログアウト（401エラー）など、
 * アプリ全体で監視可能な認証関連イベントを発行する。
 */
@Singleton
class AuthEventManager @Inject constructor() {

    /**
     * グローバルに発生しうる認証イベント。
     */
    sealed interface AuthEvent {
        /**
         * ユーザーセッションが期限切れになった際に発行される（401 Unauthorized）。
         * 監視者はログイン画面へ遷移すべき。
         */
        data object SessionExpired : AuthEvent
    }

    private val _events = MutableSharedFlow<AuthEvent>(replay = 0, extraBufferCapacity = 1)

    /**
     * 認証イベントのFlow。
     * セッション期限切れなどのグローバルな認証状態変化を処理するために監視する。
     */
    val events = _events.asSharedFlow()

    /**
     * セッション期限切れイベントを発行する。
     * 401レスポンスを受信した際に[TokenAuthenticator]から呼び出される。
     */
    fun emitSessionExpired() {
        _events.tryEmit(AuthEvent.SessionExpired)
    }
}
