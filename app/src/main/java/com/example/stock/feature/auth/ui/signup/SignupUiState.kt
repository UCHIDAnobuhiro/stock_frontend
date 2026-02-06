package com.example.stock.feature.auth.ui.signup

import androidx.annotation.StringRes

/**
 * サインアップ画面の状態を保持するデータクラス。
 *
 * @property email 入力されたメールアドレス
 * @property password 入力されたパスワード
 * @property confirmPassword 入力された確認用パスワード
 * @property isPasswordVisible パスワード表示/非表示の切り替えフラグ
 * @property isConfirmPasswordVisible 確認用パスワード表示/非表示の切り替えフラグ
 * @property isLoading サインアップ処理中かどうか
 * @property errorResId エラーメッセージのリソースID（エラーがない場合はnull）
 */
data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @get:StringRes val errorResId: Int? = null,
)

/**
 * [com.example.stock.feature.auth.viewmodel.SignupViewModel]から発行される一度きりのイベント。
 *
 * 画面遷移など、一度だけ処理すべきアクションに使用する。
 */
sealed interface SignupUiEvent {
    data object SignedUp : SignupUiEvent
}
