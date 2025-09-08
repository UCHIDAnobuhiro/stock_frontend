package com.example.stock.data.model

import kotlinx.serialization.Serializable

/**
 * ログインリクエストを表すデータクラス
 *
 * @property email ログインするユーザー名
 * @property password ログインするユーザーのパスワード
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
