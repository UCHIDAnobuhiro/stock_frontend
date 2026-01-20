package com.example.stock.feature.auth.ui.login

import androidx.annotation.StringRes

/**
 * Data class holding the state of the login screen.
 *
 * @property email Entered email address
 * @property password Entered password
 * @property isPasswordVisible Flag for password visibility toggle
 * @property isLoading Whether login process is in progress
 * @property errorResId Error message resource ID (null if no error)
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @StringRes val errorResId: Int? = null,
)

/**
 * One-time events emitted by [com.example.stock.feature.auth.viewmodel.LoginViewModel].
 *
 * Used for navigation or actions that should only be handled once.
 */
sealed interface LoginUiEvent {
    data object LoggedIn : LoginUiEvent
}
