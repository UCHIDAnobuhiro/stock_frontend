package com.example.stock.feature.auth.data.remote

import kotlinx.serialization.Serializable

/**
 * Data class representing a signup request
 *
 * @property email User email address
 * @property password User password
 */
@Serializable
data class SignupRequest(
    val email: String,
    val password: String
)
