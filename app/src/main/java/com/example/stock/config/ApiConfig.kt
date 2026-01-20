package com.example.stock.config

import com.example.stock.BuildConfig

/**
 * Configuration object for API settings.
 *
 * Provides centralized access to API configuration values from BuildConfig.
 * Different build variants (debug, staging, release) have different BASE_URL values.
 */
object ApiConfig {
    const val BASE_URL: String = BuildConfig.BASE_URL
}