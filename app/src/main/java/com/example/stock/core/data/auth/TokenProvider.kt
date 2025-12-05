package com.example.stock.core.data.auth

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
}

/**
 * TokenProvider implementation that manages tokens only in memory.
 * Uses thread-safe @Volatile variable to hold the token.
 */
class InMemoryTokenProvider : TokenProvider {
    @Volatile
    private var token: String? = null // Token in memory

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
}