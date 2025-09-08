package com.example.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException


/**
 * ログイン画面の状態を保持するデータクラス。
 *
 * @property email 入力されたメールアドレス
 * @property password 入力されたパスワード
 * @property isPasswordVisible パスワード表示/非表示フラグ
 * @property isLoading ログイン処理中かどうか
 * @property error エラーメッセージ（null の場合はエラーなし）
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ログイン処理およびログイン画面の状態を管理する [ViewModel]。
 *
 * - 入力値の検証
 * - [AuthRepository] を利用したログイン処理
 * - [LoginUiState] の更新
 *
 * @param repo 認証リポジトリ。ログインAPIを呼び出す責務を持つ。
 */
class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    sealed interface UiEvent {
        data object LoggedIn : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * メールアドレス入力変更時に状態を更新する。
     * @param email 入力されたメールアドレス
     */
    fun onEmailChange(email: String) {
        _ui.update { it.copy(email = email, error = null) }
    }

    /**
     * パスワード入力変更時に状態を更新する。
     * @param password 入力されたパスワード
     */
    fun onPasswordChange(password: String) {
        _ui.update { it.copy(password = password, error = null) }
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
     * 入力チェック後、[AuthRepository] を通じてログインを行い、
     * 成功/失敗に応じて [LoginUiState] を更新する。
     */
    fun login() {
        // 連打防止
        if (_ui.value.isLoading) return

        val (email, password) = _ui.value.let { it.email to it.password }

        validate(email, password)?.let { msg ->
            _ui.update { it.copy(error = msg) }
            return
        }

        // 非同期でログイン処理
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.login(email, password) }
                .onSuccess { _events.emit(UiEvent.LoggedIn) }
                .onFailure { e ->
                    val msg = when (e) {
                        is HttpException ->
                            if (e.code() == 401) "メールアドレスまたはパスワードが間違っています" else "HTTPエラー: ${e.code()}"

                        is IOException -> "通信エラー: ネットワークを確認してください"
                        is SerializationException -> "JSONエラー: レスポンス形式が不正です"
                        else -> "不明なエラー: ${e.message}"
                    }
                    _ui.update { it.copy(error = msg) }
                }
            _ui.update { it.copy(isLoading = false) }
        }
    }

    /**
     * validationチェック
     *
     * @param email 入力されたメールアドレス
     * @param password 入力されたパスワード
     */
    private fun validate(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "メールアドレスとパスワードを入力してください"
        !email.contains("@") -> "メールアドレスの形式が不正です"
        password.length < 8 -> "パスワードは8文字以上"
        else -> null
    }

    /**
     * ログアウト処理を実行
     */
    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }
}