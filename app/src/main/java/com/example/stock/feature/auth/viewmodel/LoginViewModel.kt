package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.login.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Manages login processing and login screen state for [ViewModel].
 *
 * - Validates input values
 * - Performs login processing using [AuthRepository]
 * - Updates [LoginUiState]
 *
 * @param repo Authentication repository responsible for calling the login API.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    sealed interface UiEvent {
        data object LoggedIn : UiEvent
        data object LoggedOut : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Updates state when email input changes.
     * @param email The entered email address
     */
    fun onEmailChange(email: String) {
        _ui.update { it.copy(email = email, errorResId = null) }
    }

    /**
     * Updates state when password input changes.
     * @param password The entered password
     */
    fun onPasswordChange(password: String) {
        _ui.update { it.copy(password = password, errorResId = null) }
    }

    /**
     * Toggles password visibility on/off.
     */
    fun togglePassword() {
        _ui.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Executes the login process.
     *
     * After validating inputs, performs login via [AuthRepository],
     * and updates [LoginUiState] based on success/failure.
     */
    fun login() {
        // Prevent multiple rapid clicks
        if (_ui.value.isLoading) return

        val (email, password) = _ui.value.let { it.email to it.password }

        validate(email, password)?.let { errorResId ->
            _ui.update { it.copy(errorResId = errorResId) }
            return
        }

        // Perform login asynchronously
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorResId = null) }
            runCatching { repo.login(email, password) }
                .onSuccess { _events.emit(UiEvent.LoggedIn) }
                .onFailure { e ->
                    val logMessage = when (e) {
                        is HttpException -> "HTTP error: ${e.code()} - ${e.message()}"
                        is IOException -> "Network error: ${e.message}"
                        is SerializationException -> "JSON parse error: ${e.message}"
                        else -> "Unknown error: ${e.message}"
                    }
                    Timber.e(e, "Login failed: $logMessage")
                    _ui.update { it.copy(errorResId = R.string.error_login_failed) }
                }
            _ui.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Performs validation check
     *
     * @param email The entered email address
     * @param password The entered password
     * @return Error message resource ID if validation fails, null if valid
     */
    private fun validate(email: String, password: String): Int? = when {
        email.isBlank() || password.isBlank() -> R.string.error_empty_fields
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches() -> R.string.error_invalid_email

        password.length < 8 -> R.string.error_password_too_short
        else -> null
    }

    /**
     * Executes logout processing and resets UI state to initial values.
     * Emits [UiEvent.LoggedOut] event upon completion.
     */
    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _ui.value = LoginUiState()
            _events.emit(UiEvent.LoggedOut)
        }
    }
}
