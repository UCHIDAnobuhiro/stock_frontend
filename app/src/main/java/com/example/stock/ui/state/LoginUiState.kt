package com.example.stock.ui.state

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
