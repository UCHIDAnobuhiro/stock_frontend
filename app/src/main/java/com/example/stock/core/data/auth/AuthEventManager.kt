package com.example.stock.core.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global authentication event manager.
 *
 * Emits authentication-related events that can be observed app-wide,
 * such as forced logout due to token expiration (401 errors).
 */
@Singleton
class AuthEventManager @Inject constructor() {

    /**
     * Authentication events that can occur globally.
     */
    sealed interface AuthEvent {
        /**
         * Emitted when the user's session has expired (401 Unauthorized).
         * Observers should navigate to the login screen.
         */
        data object SessionExpired : AuthEvent
    }

    private val _events = MutableSharedFlow<AuthEvent>(replay = 0, extraBufferCapacity = 1)

    /**
     * Flow of authentication events.
     * Observe this to handle global auth state changes like session expiration.
     */
    val events = _events.asSharedFlow()

    /**
     * Emits a session expired event.
     * Called by [TokenAuthenticator] when a 401 response is received.
     */
    fun emitSessionExpired() {
        _events.tryEmit(AuthEvent.SessionExpired)
    }
}
