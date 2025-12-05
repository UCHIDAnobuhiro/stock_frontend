package com.example.stock.feature.auth.data.remote

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
