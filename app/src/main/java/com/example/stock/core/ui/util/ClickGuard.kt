package com.example.stock.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Composable utility to prevent rapid button clicks.
 *
 * Usage example:
 * val canClick = rememberClickGuard()
 * if (canClick()) { onClick() }
 *
 * @param delayMillis Cooldown period in milliseconds before next click is allowed
 * @return Function that returns true if click is allowed, false if still in cooldown
 */
@Composable
fun rememberClickGuard(delayMillis: Long = 500L): (() -> Boolean) {
    var enabled by remember { mutableStateOf(true) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            kotlinx.coroutines.delay(delayMillis)
            enabled = true
        }
    }

    return {
        if (enabled) {
            enabled = false
            true
        } else false
    }
}