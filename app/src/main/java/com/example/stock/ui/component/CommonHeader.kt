package com.example.stock.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.stock.R
import com.example.stock.ui.util.rememberClickGuard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonHeader(
    titleText: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val canClick = rememberClickGuard()

    TopAppBar(
        title = { Text(titleText) },
        navigationIcon = {
            IconButton(
                onClick = { if (canClick()) onBack() },
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_btn_text)
                )
            }
        },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout)
                )
            }
        }
    )
}