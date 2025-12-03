package com.example.stock.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * ボタン連打防止のためのComposableユーティリティ。
 *
 * 使用例：
 * val canClick = rememberClickGuard()
 * if (canClick()) { onClick() }
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