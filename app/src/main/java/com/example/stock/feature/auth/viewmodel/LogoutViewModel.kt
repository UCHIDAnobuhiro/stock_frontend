package com.example.stock.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.viewmodel.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel responsible for logout operations.
 *
 * Manages the logout process by clearing tokens from both memory and persistent storage.
 * Emits [UiEvent.LoggedOut] upon successful logout to trigger navigation.
 */
@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    sealed interface UiEvent {
        data object LoggedOut : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Executes logout processing.
     *
     * Clears tokens from memory and persistent storage via [AuthRepository],
     * then emits [UiEvent.LoggedOut] to trigger navigation to login screen.
     */
    fun logout() {
        viewModelScope.launch(dispatcherProvider.main) {
            try {
                withContext(dispatcherProvider.io) {
                    repo.logout()
                }
            } catch (e: Exception) {
                // Log error but continue - user intends to logout regardless of failure
                ErrorHandler.logError(e, "Logout")
            }
            // Always emit LoggedOut to navigate to login screen
            _events.emit(UiEvent.LoggedOut)
        }
    }
}
