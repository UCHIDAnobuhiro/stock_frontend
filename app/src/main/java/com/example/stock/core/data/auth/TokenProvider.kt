package com.example.stock.core.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Interface for obtaining, updating, and clearing access tokens.
 */
interface TokenProvider {
    /**
     * Retrieves the currently held token.
     * @return The held token, or null if none exists.
     */
    fun getToken(): String?

    /**
     * Updates the token with a new value.
     * @param token The new token
     */
    fun update(token: String)

    /**
     * Clears the held token.
     */
    fun clear()

    /**
     * StateFlow indicating whether token restoration from storage is complete.
     */
    val isRestorationComplete: StateFlow<Boolean>

    /**
     * Marks token restoration as complete.
     * Called by Application after restoring token from persistent storage.
     */
    fun markRestorationComplete()

    /**
     * Suspends until token restoration is complete.
     */
    suspend fun awaitRestoration()
}

/**
 * TokenProvider implementation that manages tokens only in memory.
 * Uses thread-safe @Volatile variable to hold the token.
 */
class InMemoryTokenProvider : TokenProvider {
    @Volatile
    private var token: String? = null

    private val _isRestorationComplete = MutableStateFlow(false)
    override val isRestorationComplete: StateFlow<Boolean> = _isRestorationComplete.asStateFlow()

    /**
     * Returns the held token.
     */
    override fun getToken(): String? = token

    /**
     * Overwrites the token with a new value.
     */
    override fun update(token: String) {
        this.token = token
    }

    /**
     * Clears the token.
     */
    override fun clear() {
        this.token = null
    }

    /**
     * Marks token restoration as complete.
     */
    override fun markRestorationComplete() {
        _isRestorationComplete.value = true
    }

    /**
     * Suspends until token restoration is complete.
     */
    override suspend fun awaitRestoration() {
        _isRestorationComplete.first { it }
    }
}