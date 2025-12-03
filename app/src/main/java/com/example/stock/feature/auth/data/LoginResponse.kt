package com.example.stock.feature.auth.data

import kotlinx.serialization.Serializable

/**
 * ログインレスポンスを表すデータクラス
 *
 * @property token JWTのトークン
 */
@Serializable
data class LoginResponse(
    val token: String
)
