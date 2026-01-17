package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.login.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Manages login processing and login screen state for [ViewModel].
 *
 * - Validates input values
 * - Performs login processing using [AuthRepository]
 * - Updates [LoginUiState]
 *
 * @param repo Authentication repository responsible for calling the login API.
 * @param dispatcherProvider Provider for coroutine dispatchers, enabling testability.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

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

        InputValidator.validateLogin(email, password)?.let { errorResId ->
            _ui.update { it.copy(errorResId = errorResId) }
            return
        }

        // Perform login asynchronously
        viewModelScope.launch(dispatcherProvider.main) {
            _ui.update { it.copy(isLoading = true, errorResId = null) }
            try {
                runCatching {
                    withContext(dispatcherProvider.io) {
                        repo.login(email, password)
                    }
                }
                    .onSuccess { _events.emit(UiEvent.LoggedIn) }
                    .onFailure { e ->
                        ErrorHandler.logError(e, "Login")
                        _ui.update { it.copy(errorResId = R.string.error_login_failed) }
                    }
            } finally {
                _ui.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Checks if user is already authenticated.
     * Waits for token restoration from storage to complete before checking.
     * If a valid token exists, emits [UiEvent.LoggedIn] to navigate to main screen.
     * Called on app startup to enable auto-login.
     */
    fun checkAuthState() {
        viewModelScope.launch(dispatcherProvider.main) {
            val hasToken = withContext(dispatcherProvider.io) {
                repo.hasToken()
            }
            if (hasToken) {
                _events.emit(UiEvent.LoggedIn)
            }
        }
    }
}
