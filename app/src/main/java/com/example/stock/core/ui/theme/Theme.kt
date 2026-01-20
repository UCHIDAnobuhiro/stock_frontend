package com.example.stock.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * App-wide Material3 theme composable.
 *
 * Applies dynamic color schemes based on system theme preference.
 * Uses [dynamicDarkColorScheme] for dark mode and [dynamicLightColorScheme] for light mode.
 *
 * @param darkTheme Whether to use dark theme, defaults to system preference
 * @param content The composable content to be themed
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