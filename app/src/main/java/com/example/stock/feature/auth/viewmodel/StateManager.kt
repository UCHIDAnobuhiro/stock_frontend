package com.example.stock.feature.auth.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Utility object for managing UI state in ViewModels.
 *
 * Provides helper functions for consistent and safe state management,
 * particularly for loading states and error handling.
 */
object StateManager {

    /**
     * Executes an async operation while managing loading state.
     *
     * This function ensures that:
     * - Loading is set to true before the operation starts
     * - Error state is cleared when loading starts
     * - Loading is set to false after the operation completes (even if it fails)
     * - Prevents execution if already loading
     *
     * @param T The UI state type
     * @param stateFlow The MutableStateFlow containing the UI state
     * @param isLoading A function to check if currently loading
     * @param setLoading A function to create a new state with the specified loading value
     * @param operation The async operation to execute
     * @return true if operation was executed, false if skipped due to already loading
     */
    suspend inline fun <T> executeWithLoading(
        stateFlow: MutableStateFlow<T>,
        crossinline isLoading: T.() -> Boolean,
        crossinline setLoading: T.(Boolean) -> T,
        crossinline operation: suspend () -> Unit
    ): Boolean {
        // Prevent multiple rapid clicks
        if (stateFlow.value.isLoading()) return false

        try {
            stateFlow.update { it.setLoading(true) }
            operation()
        } finally {
            stateFlow.update { it.setLoading(false) }
        }
        return true
    }
}
