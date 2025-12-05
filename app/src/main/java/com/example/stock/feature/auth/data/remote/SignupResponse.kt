package com.example.stock.feature.auth.data.remote

import kotlinx.serialization.Serializable

/**
 * Data class representing a signup response
 *
 * @property message Response message
 */
@Serializable
data class SignupResponse(
    val message: String
)