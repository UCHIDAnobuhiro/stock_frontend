package com.example.stock.feature.auth.data.remote

import kotlinx.serialization.Serializable

/**
 * Data class representing a login response
 *
 * @property token JWT authentication token
 */
@Serializable
data class LoginResponse(
    val token: String
)
