package com.example.stock.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.stock.R

/**
 * Common top bar (header) for the app.
 *
 * Displays a title and logout button.
 *
 * @param titleText Title string to display in the header
 * @param onLogout Callback when logout button is pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeader(
    titleText: String,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = { Text(titleText) }, // Display title
        actions = {
            // Logout button (icon)
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout)
                )
            }
        }
    )
}