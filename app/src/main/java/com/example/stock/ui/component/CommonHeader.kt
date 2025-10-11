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

/**
 * アプリ共通のトップバー（ヘッダー）。
 *
 * タイトル、戻るボタン、ログアウトボタンを表示する。
 * 戻るボタンは連打防止のガード付き。
 *
 * @param titleText ヘッダーに表示するタイトル文字列
 * @param onBack 戻るボタン押下時のコールバック
 * @param onLogout ログアウトボタン押下時のコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonHeader(
    titleText: String,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val canClick = rememberClickGuard() // 連打防止用ガード

    TopAppBar(
        title = { Text(titleText) }, // タイトル表示
        navigationIcon = {
            // 戻るボタン（ガード付き）
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
            // ログアウトボタン（アイコン）
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.logout)
                )
            }
        }
    )
}