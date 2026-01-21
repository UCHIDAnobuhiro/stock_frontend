package com.example.stock.feature.auth.ui.signup

/**
 * Data class holding the state of the signup screen.
 *
 * @property email Entered email address
 * @property password Entered password
 * @property confirmPassword Entered confirm password
 * @property isPasswordVisible Flag for password visibility toggle
 * @property isConfirmPasswordVisible Flag for confirm password visibility toggle
 * @property isLoading Whether signup process is in progress
 * @property errorResId Error message resource ID (null if no error)
 */
data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorResId: Int? = null,
)

/**
 * One-time events emitted by [com.example.stock.feature.auth.viewmodel.SignupViewModel].
 *
 * Used for navigation or actions that should only be handled once.
 */
sealed interface SignupUiEvent {
    data object SignedUp : SignupUiEvent
}
