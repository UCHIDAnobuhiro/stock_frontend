package com.example.stock.feature.auth.viewmodel

import android.util.Patterns
import com.example.stock.R

/**
 * 認証フローにおけるユーザー入力を検証するユーティリティオブジェクト。
 *
 * メール、パスワード、その他の入力フィールドの再利用可能なバリデーションロジックを提供する。
 */
object InputValidator {

    private const val MIN_PASSWORD_LENGTH = 8

    /**
     * ログイン用のメールとパスワードを検証する。
     *
     * @param email 検証するメールアドレス
     * @param password 検証するパスワード
     * @return バリデーション失敗時はエラーメッセージのリソースID、有効な場合はnull
     */
    fun validateLogin(email: String, password: String): Int? = when {
        email.isBlank() || password.isBlank() -> R.string.error_empty_fields
        !isValidEmail(email) -> R.string.error_invalid_email
        !isValidPasswordLength(password) -> R.string.error_password_too_short
        else -> null
    }

    /**
     * サインアップ用のメール、パスワード、確認用パスワードを検証する。
     *
     * @param email 検証するメールアドレス
     * @param password 検証するパスワード
     * @param confirmPassword 検証する確認用パスワード
     * @return バリデーション失敗時はエラーメッセージのリソースID、有効な場合はnull
     */
    fun validateSignup(email: String, password: String, confirmPassword: String): Int? = when {
        email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> R.string.error_empty_fields
        !isValidEmail(email) -> R.string.error_invalid_email
        !isValidPasswordLength(password) -> R.string.error_password_too_short
        !doPasswordsMatch(password, confirmPassword) -> R.string.error_passwords_do_not_match
        else -> null
    }

    /**
     * Androidの組み込みメールパターンマッチャーを使用してメール形式を検証する。
     *
     * @param email 検証するメールアドレス
     * @return メール形式が有効な場合はtrue、それ以外はfalse
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * パスワードの長さが最小要件を満たしているかを検証する。
     *
     * @param password 検証するパスワード
     * @return パスワードの長さが有効な場合はtrue、それ以外はfalse
     */
    fun isValidPasswordLength(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * パスワードと確認用パスワードが一致するかを確認する。
     *
     * @param password パスワード
     * @param confirmPassword 確認用パスワード
     * @return パスワードが一致する場合はtrue、それ以外はfalse
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

}
