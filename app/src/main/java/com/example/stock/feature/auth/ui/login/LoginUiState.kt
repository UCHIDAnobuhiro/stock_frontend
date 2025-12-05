package com.example.stock.feature.auth.ui.login

/**
 * Data class holding the state of the login/signup screen.
 *
 * @property email Entered email address
 * @property password Entered password
 * @property confirmPassword Entered confirm password (for signup)
 * @property isPasswordVisible Flag for password visibility toggle
 * @property isConfirmPasswordVisible Flag for confirm password visibility toggle
 * @property isLoading Whether login/signup process is in progress
 * @property error Error message (null if no error)
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
