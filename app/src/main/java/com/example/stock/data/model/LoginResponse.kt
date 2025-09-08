package com.example.stock.data.model

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
