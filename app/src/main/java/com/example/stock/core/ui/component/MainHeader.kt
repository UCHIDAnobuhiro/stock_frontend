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
 * アプリ共通のトップバー（ヘッダー）。
 *
 * タイトルとログアウトボタンを表示する。
 *
 * @param titleText ヘッダーに表示するタイトル文字列
 * @param onLogout ログアウトボタン押下時のコールバック
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHeader(
    titleText: String,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = { Text(titleText) }, // タイトル表示
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