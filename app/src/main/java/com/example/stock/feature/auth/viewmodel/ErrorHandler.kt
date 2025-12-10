package com.example.stock.feature.auth.viewmodel

import androidx.annotation.StringRes
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * Utility object for handling authentication-related errors.
 *
 * Provides common error handling logic for authentication operations,
 * including logging and error message mapping.
 */
object ErrorHandler {

    /**
     * Handles authentication-related errors by logging them and mapping to user-friendly error messages.
     *
     * @param exception The exception that occurred during authentication
     * @param operation A string describing the operation that failed (e.g., "Login", "Signup")
     * @param errorMapper A function that maps specific error types to string resource IDs
     * @return String resource ID for the error message to display to the user
     */
    fun handleAuthError(
        exception: Throwable,
        operation: String,
        errorMapper: (Throwable) -> Int
    ): Int {
        val logMessage = when (exception) {
            is HttpException -> "HTTP error: ${exception.code()} - ${exception.message()}"
            is IOException -> "Network error: ${exception.message}"
            is SerializationException -> "JSON parse error: ${exception.message}"
            else -> "Unknown error: ${exception.message}"
        }
        Timber.e(exception, "$operation failed: $logMessage")

        return errorMapper(exception)
    }

    /**
     * Logs the error with a simple message format.
     *
     * @param exception The exception to log
     * @param operation A string describing the operation that failed
     */
    fun logError(exception: Throwable, operation: String) {
        val logMessage = when (exception) {
            is HttpException -> "HTTP error: ${exception.code()} - ${exception.message()}"
            is IOException -> "Network error: ${exception.message}"
            is SerializationException -> "JSON parse error: ${exception.message}"
            else -> "Unknown error: ${exception.message}"
        }
        Timber.e(exception, "$operation failed: $logMessage")
    }

    /**
     * Maps an exception to a string resource ID based on its type.
     *
     * @param exception The exception to map
     * @param httpErrorMapper A function that maps HTTP status codes to string resource IDs
     * @param defaultErrorResId The default error resource ID to use if no specific mapping exists
     * @return String resource ID for the error message
     */
    fun mapErrorToResource(
        exception: Throwable,
        httpErrorMapper: ((HttpException) -> Int)? = null,
        @StringRes defaultErrorResId: Int
    ): Int {
        return when (exception) {
            is HttpException -> httpErrorMapper?.invoke(exception) ?: defaultErrorResId
            else -> defaultErrorResId
        }
    }
}
