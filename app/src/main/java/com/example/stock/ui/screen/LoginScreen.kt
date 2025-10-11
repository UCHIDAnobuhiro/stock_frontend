package com.example.stock.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.stock.R
import com.example.stock.viewmodel.AuthViewModel

/**
 * ログイン画面。
 *
 * メールアドレス・パスワード入力、パスワード表示切替、
 * ログインボタン、エラー表示、進捗インジケータなどを提供する。
 *
 * @param viewModel 認証用ViewModel
 * @param onLoggedIn ログイン成功時のコールバック
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
) {
    val ui by viewModel.ui.collectAsState()

    // ログイン成功時に画面遷移（1回だけ）
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AuthViewModel.UiEvent.LoggedIn) {
                onLoggedIn()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // タイトル
        Text(stringResource(R.string.login), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        // メールアドレス入力欄
        OutlinedTextField(
            value = ui.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text(stringResource(R.string.mail)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // パスワード入力欄（表示切替アイコン付き）
        OutlinedTextField(
            value = ui.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = if (ui.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = viewModel::togglePassword) {
                    Icon(
                        imageVector = if (ui.isPasswordVisible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = if (ui.isPasswordVisible) stringResource(R.string.hide) else stringResource(
                            R.string.show
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // エラー表示
        if (ui.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(ui.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        // ログインボタン（進捗インジケータ付き）
        Button(
            onClick = viewModel::login,
            enabled = !ui.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (ui.isLoading) CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            else Text(stringResource(R.string.login))
        }
    }
}