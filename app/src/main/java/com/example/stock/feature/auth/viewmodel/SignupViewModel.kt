package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.signup.SignupUiState
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
 * Manages signup processing and signup screen state for [ViewModel].
 *
 * - Validates input values
 * - Performs signup processing using [AuthRepository]
 * - Updates [SignupUiState]
 *
 * @param repo Authentication repository responsible for calling the signup API.
 * @param dispatcherProvider Provider for coroutine dispatchers, enabling testability.
 */
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(SignupUiState())
    val ui: StateFlow<SignupUiState> = _ui

    /**
     * UI events emitted by SignupViewModel for one-time actions.
     */
    sealed interface UiEvent {
        /** Emitted when signup completes successfully. */
        data object SignedUp : UiEvent
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
     * Updates state when confirm password input changes.
     * @param confirmPassword The entered confirm password
     */
    fun onConfirmPasswordChange(confirmPassword: String) {
        _ui.update { it.copy(confirmPassword = confirmPassword, errorResId = null) }
    }

    /**
     * Toggles confirm password visibility on/off.
     */
    fun toggleConfirmPassword() {
        _ui.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    /**
     * Executes the signup process.
     *
     * After validating inputs, performs signup via [AuthRepository],
     * and updates [SignupUiState] based on success/failure.
     */
    fun signup() {
        // Prevent multiple rapid clicks
        if (_ui.value.isLoading) return

        val (email, password, confirmPassword) = _ui.value.let {
            Triple(it.email, it.password, it.confirmPassword)
        }

        InputValidator.validateSignup(email, password, confirmPassword)?.let { errorResId ->
            _ui.update { it.copy(errorResId = errorResId) }
            return
        }

        // Perform signup asynchronously
        viewModelScope.launch(dispatcherProvider.main) {
            _ui.update { it.copy(isLoading = true, errorResId = null) }
            try {
                runCatching {
                    withContext(dispatcherProvider.io) {
                        repo.signup(email, password)
                    }
                }
                    .onSuccess { _events.emit(UiEvent.SignedUp) }
                    .onFailure { e ->
                        ErrorHandler.logError(e, "Signup")
                        val errorResId = ErrorHandler.mapErrorToResource(
                            exception = e,
                            httpErrorMapper = { httpException ->
                                if (httpException.code() == 409) R.string.error_email_already_registered
                                else R.string.error_signup_failed
                            },
                            defaultErrorResId = R.string.error_signup_failed
                        )
                        _ui.update { it.copy(errorResId = errorResId) }
                    }
            } finally {
                _ui.update { it.copy(isLoading = false) }
            }
        }
    }
}
