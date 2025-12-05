package com.example.stock.feature.auth.data.remote

import kotlinx.serialization.Serializable

/**
 * Authentication-related data transfer objects (DTOs).
 * Contains request and response models for login and signup operations.
 */

// ===== Login =====

/**
 * Data class representing a login request.
 *
 * @property email User email address
 * @property password User password
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Data class representing a login response.
 *
 * @property token JWT authentication token
 */
@Serializable
data class LoginResponse(
    val token: String
)

// ===== Signup =====

/**
 * Data class representing a signup request.
 *
 * @property email User email address
 * @property password User password
 */
@Serializable
data class SignupRequest(
    val email: String,
    val password: String
)

/**
 * Data class representing a signup response.
 *
 * @property message Response message
 */
@Serializable
data class SignupResponse(
    val message: String
)
