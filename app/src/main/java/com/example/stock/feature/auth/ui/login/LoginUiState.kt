package com.example.stock.feature.auth.ui.login

/**
 * Data class holding the state of the login screen.
 *
 * @property email Entered email address
 * @property password Entered password
 * @property isPasswordVisible Flag for password visibility toggle
 * @property isLoading Whether login process is in progress
 * @property error Error message (null if no error)
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
