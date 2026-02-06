package com.example.stock.feature.auth.data.remote

import kotlinx.serialization.Serializable

/**
 * 認証関連のデータ転送オブジェクト（DTO）。
 * ログインとサインアップ操作のリクエスト・レスポンスモデルを含む。
 */

// ===== ログイン =====

/**
 * ログインリクエストを表すデータクラス。
 *
 * @property email ユーザーのメールアドレス
 * @property password ユーザーのパスワード
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * ログインレスポンスを表すデータクラス。
 *
 * @property token JWT認証トークン
 */
@Serializable
data class LoginResponse(
    val token: String
)

// ===== サインアップ =====

/**
 * サインアップリクエストを表すデータクラス。
 *
 * @property email ユーザーのメールアドレス
 * @property password ユーザーのパスワード
 */
@Serializable
data class SignupRequest(
    val email: String,
    val password: String
)

/**
 * サインアップレスポンスを表すデータクラス。
 *
 * @property message レスポンスメッセージ
 */
@Serializable
data class SignupResponse(
    val message: String
)
