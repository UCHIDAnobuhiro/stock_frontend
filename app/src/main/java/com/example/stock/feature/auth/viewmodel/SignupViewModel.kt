package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.signup.SignupUiEvent
import com.example.stock.feature.auth.ui.signup.SignupUiState
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
 * サインアップ処理とサインアップ画面状態を管理する[ViewModel]。
 *
 * - 入力値のバリデーション
 * - [AuthRepository]を使用したサインアップ処理の実行
 * - [SignupUiState]の更新
 *
 * @param repo サインアップAPIを呼び出す認証リポジトリ
 * @param dispatcherProvider コルーチンディスパッチャーのプロバイダー。テスト容易性を実現。
 */
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(SignupUiState())
    val ui: StateFlow<SignupUiState> = _ui

    private val _events = MutableSharedFlow<SignupUiEvent>(replay = 0, extraBufferCapacity = 1)
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
     * 確認用パスワード入力変更時に状態を更新する。
     * @param confirmPassword 入力された確認用パスワード
     */
    fun onConfirmPasswordChange(confirmPassword: String) {
        _ui.update { it.copy(confirmPassword = confirmPassword, errorResId = null) }
    }

    /**
     * 確認用パスワードの表示/非表示を切り替える。
     */
    fun toggleConfirmPassword() {
        _ui.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    /**
     * サインアップ処理を実行する。
     *
     * 入力値をバリデーション後、[AuthRepository]経由でサインアップを実行し、
     * 成功/失敗に応じて[SignupUiState]を更新する。
     */
    fun signup() {
        // 連打防止
        if (_ui.value.isLoading) return

        val (email, password, confirmPassword) = _ui.value.let {
            Triple(it.email.trim(), it.password, it.confirmPassword)
        }

        InputValidator.validateSignup(email, password, confirmPassword)?.let { errorResId ->
            _ui.update { it.copy(errorResId = errorResId) }
            return
        }

        // 非同期でサインアップを実行
        viewModelScope.launch(dispatcherProvider.main) {
            _ui.update { it.copy(isLoading = true, errorResId = null) }
            runCatching {
                withContext(dispatcherProvider.io) {
                    repo.signup(email, password)
                }
            }
                .onSuccess { _events.emit(SignupUiEvent.SignedUp) }
                .onFailure { e ->
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    ErrorHandler.logError(e, "Signup")
                    val errorResId = ErrorHandler.mapErrorToResource(
                        exception = e,
                        httpErrorMapper = { httpException ->
                            if (httpException.code() == 409) R.string.error_email_already_registered
                            else R.string.error_signup_failed
                        },
                        defaultErrorResId = R.string.error_signup_failed
                    )
                    _ui.update { it.copy(errorResId = errorResId) }
                }
            _ui.update { it.copy(isLoading = false) }
        }
    }
}
