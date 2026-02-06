package com.example.stock.feature.auth.ui.login

import androidx.annotation.StringRes

/**
 * ログイン画面の状態を保持するデータクラス。
 *
 * @property email 入力されたメールアドレス
 * @property password 入力されたパスワード
 * @property isPasswordVisible パスワード表示/非表示の切り替えフラグ
 * @property isLoading ログイン処理中かどうか
 * @property errorResId エラーメッセージのリソースID（エラーがない場合はnull）
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @get:StringRes val errorResId: Int? = null,
)

/**
 * [com.example.stock.feature.auth.viewmodel.LoginViewModel]から発行される一度きりのイベント。
 *
 * 画面遷移など、一度だけ処理すべきアクションに使用する。
 */
sealed interface LoginUiEvent {
    data object LoggedIn : LoginUiEvent
}
