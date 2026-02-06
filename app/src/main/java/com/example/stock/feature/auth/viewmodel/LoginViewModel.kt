package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.login.LoginUiEvent
import com.example.stock.feature.auth.ui.login.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ログイン処理とログイン画面状態を管理する[ViewModel]。
 *
 * - 入力値のバリデーション
 * - [AuthRepository]を使用したログイン処理の実行
 * - [LoginUiState]の更新
 *
 * @param repo ログインAPIを呼び出す認証リポジトリ
 * @param dispatcherProvider コルーチンディスパッチャーのプロバイダー。テスト容易性を実現。
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    private val _events = MutableSharedFlow<LoginUiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * メール入力変更時に状態を更新する。
     * @param email 入力されたメールアドレス
     */
    fun onEmailChange(email: String) {
        _ui.update { it.copy(email = email, errorResId = null) }
    }

    /**
     * パスワード入力変更時に状態を更新する。
     * @param password 入力されたパスワード
     */
    fun onPasswordChange(password: String) {
        _ui.update { it.copy(password = password, errorResId = null) }
    }

    /**
     * パスワードの表示/非表示を切り替える。
     */
    fun togglePassword() {
        _ui.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * ログイン処理を実行する。
     *
     * 入力値をバリデーション後、[AuthRepository]経由でログインを実行し、
     * 成功/失敗に応じて[LoginUiState]を更新する。
     */
    fun login() {
        // 連打防止
        if (_ui.value.isLoading) return

        val (email, password) = _ui.value.let { it.email.trim() to it.password }

        InputValidator.validateLogin(email, password)?.let { errorResId ->
            _ui.update { it.copy(errorResId = errorResId) }
            return
        }

        // 非同期でログインを実行
        viewModelScope.launch(dispatcherProvider.main) {
            _ui.update { it.copy(isLoading = true, errorResId = null) }
            runCatching {
                withContext(dispatcherProvider.io) {
                    repo.login(email, password)
                }
            }
                .onSuccess { _events.emit(LoginUiEvent.LoggedIn) }
                .onFailure { e ->
                    ErrorHandler.logError(e, "Login")
                    val errorResId = ErrorHandler.mapErrorToResource(
                        exception = e,
                        httpErrorMapper = { httpEx ->
                            when (httpEx.code()) {
                                401 -> R.string.error_invalid_credentials
                                else -> R.string.error_login_failed
                            }
                        },
                        defaultErrorResId = R.string.error_login_failed
                    )
                    _ui.update { it.copy(errorResId = errorResId) }
                }
            _ui.update { it.copy(isLoading = false) }
        }
    }

    /**
     * ユーザーが既に認証済みかを確認する。
     * 確認前にストレージからのトークン復元完了を待機する。
     * 有効なトークンが存在する場合、メイン画面への遷移のため[LoginUiEvent.LoggedIn]を発行する。
     * アプリ起動時の自動ログインを有効にするために呼び出される。
     */
    fun checkAuthState() {
        viewModelScope.launch(dispatcherProvider.main) {
            val hasToken = withContext(dispatcherProvider.io) {
                repo.hasToken()
            }
            if (hasToken) {
                _events.emit(LoginUiEvent.LoggedIn)
            }
        }
    }
}
