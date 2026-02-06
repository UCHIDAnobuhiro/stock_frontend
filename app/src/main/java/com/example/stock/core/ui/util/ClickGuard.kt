package com.example.stock.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * ボタンの連打を防止するComposableユーティリティ。
 *
 * 使用例：
 * val canClick = rememberClickGuard()
 * if (canClick()) { onClick() }
 *
 * @param delayMillis 次のクリックが許可されるまでのクールダウン期間（ミリ秒）
 * @return クリックが許可されている場合はtrue、クールダウン中の場合はfalseを返す関数
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