package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.core.data.auth.AuthEventManager
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ログアウト操作を担当するViewModel。
 *
 * メモリと永続ストレージの両方からトークンをクリアしてログアウト処理を管理する。
 * ログアウト成功時に画面遷移をトリガーするため[UiEvent.LoggedOut]を発行する。
 *
 * また、グローバル認証イベント（401レスポンスによるセッション期限切れなど）を監視し、
 * ログイン画面への遷移をトリガーするため[UiEvent.LoggedOut]を発行する。
 */
@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val authEventManager: AuthEventManager
) : ViewModel() {

    /**
     * LogoutViewModelから発行される一度きりのUIイベント。
     */
    sealed interface UiEvent {
        /** ログアウト完了またはセッション期限切れ時に発行される。 */
        data object LoggedOut : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        // グローバル認証イベント（401セッション期限切れなど）を監視
        viewModelScope.launch {
            authEventManager.events.collect { event ->
                when (event) {
                    AuthEventManager.AuthEvent.SessionExpired -> {
                        _events.emit(UiEvent.LoggedOut)
                    }
                }
            }
        }
    }

    /**
     * ログアウト処理を実行する。
     *
     * [AuthRepository]経由でメモリと永続ストレージからトークンをクリアし、
     * ログイン画面への遷移をトリガーするため[UiEvent.LoggedOut]を発行する。
     */
    fun logout() {
        viewModelScope.launch(dispatcherProvider.main) {
            try {
                withContext(dispatcherProvider.io) {
                    repo.logout()
                }
            } catch (e: Exception) {
                // エラーをログ出力するが継続 - 失敗に関係なくユーザーはログアウトを意図している
                ErrorHandler.logError(e, "Logout")
            }
            // 常にLoggedOutを発行してログイン画面へ遷移
            _events.emit(UiEvent.LoggedOut)
        }
    }
}
