package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.login.LoginUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Manages login processing and login screen state for [ViewModel].
 *
 * - Validates input values
 * - Performs login processing using [AuthRepository]
 * - Updates [LoginUiState]
 *
 * @param repo Authentication repository responsible for calling the login API.
 */
class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    sealed interface UiEvent {
        data object LoggedIn : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Updates state when email input changes.
     * @param email The entered email address
     */
    fun onEmailChange(email: String) {
        _ui.update { it.copy(email = email, error = null) }
    }

    /**
     * Updates state when password input changes.
     * @param password The entered password
     */
    fun onPasswordChange(password: String) {
        _ui.update { it.copy(password = password, error = null) }
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

        validate(email, password)?.let { msg ->
            _ui.update { it.copy(error = msg) }
            return
        }

        // Perform login asynchronously
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.login(email, password) }
                .onSuccess { _events.emit(UiEvent.LoggedIn) }
                .onFailure { e ->
                    val msg = when (e) {
                        is HttpException ->
                            if (e.code() == 401) "Email address or password is incorrect" else "HTTP error: ${e.code()}"

                        is IOException -> "Network error: Please check your connection"
                        is SerializationException -> "JSON error: Invalid response format"
                        else -> "Unknown error: ${e.message}"
                    }
                    _ui.update { it.copy(error = msg) }
                }
            _ui.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Performs validation check
     *
     * @param email The entered email address
     * @param password The entered password
     */
    private fun validate(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "Please enter email address and password"
        !email.contains("@") -> "Invalid email address format"
        password.length < 8 -> "Password must be at least 8 characters"
        else -> null
    }

    /**
     * Executes logout processing
     */
    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _ui.update { it.copy(email = "", password = "") }
        }
    }
}