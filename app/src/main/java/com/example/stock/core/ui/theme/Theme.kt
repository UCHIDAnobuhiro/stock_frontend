package com.example.stock.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * アプリ全体のMaterial3テーマComposable。
 *
 * システムのテーマ設定に基づいて動的なカラースキームを適用する。
 * ダークモードには[dynamicDarkColorScheme]、ライトモードには[dynamicLightColorScheme]を使用。
 *
 * @param darkTheme ダークテーマを使用するかどうか。デフォルトはシステム設定に従う
 * @param content テーマを適用するComposableコンテンツ
 */
@Composable
fun StockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme =
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}