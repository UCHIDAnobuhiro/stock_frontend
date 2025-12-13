package com.example.stock.feature.auth.viewmodel

import android.util.Patterns
import com.example.stock.R

/**
 * Utility object for validating user input in authentication flows.
 *
 * Provides reusable validation logic for email, password, and other input fields.
 */
object InputValidator {

    private const val MIN_PASSWORD_LENGTH = 8

    /**
     * Validates email and password for login.
     *
     * @param email The email address to validate
     * @param password The password to validate
     * @return Error message resource ID if validation fails, null if valid
     */
    fun validateLogin(email: String, password: String): Int? = when {
        email.isBlank() || password.isBlank() -> R.string.error_empty_fields
        !isValidEmail(email) -> R.string.error_invalid_email
        !isValidPasswordLength(password) -> R.string.error_password_too_short
        else -> null
    }

    /**
     * Validates email, password, and confirm password for signup.
     *
     * @param email The email address to validate
     * @param password The password to validate
     * @param confirmPassword The confirm password to validate
     * @return Error message resource ID if validation fails, null if valid
     */
    fun validateSignup(email: String, password: String, confirmPassword: String): Int? = when {
        email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> R.string.error_empty_fields
        !isValidEmail(email) -> R.string.error_invalid_email
        !isValidPasswordLength(password) -> R.string.error_password_too_short
        !doPasswordsMatch(password, confirmPassword) -> R.string.error_passwords_do_not_match
        else -> null
    }

    /**
     * Validates email format using Android's built-in email pattern matcher.
     *
     * @param email The email address to validate
     * @return true if email format is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password length meets minimum requirements.
     *
     * @param password The password to validate
     * @return true if password length is valid, false otherwise
     */
    fun isValidPasswordLength(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Checks if password and confirm password match.
     *
     * @param password The password
     * @param confirmPassword The confirm password
     * @return true if passwords match, false otherwise
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

}
