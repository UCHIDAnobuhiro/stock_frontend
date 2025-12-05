package com.example.stock.feature.auth.ui.login

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stock.R
import com.example.stock.core.ui.theme.Sizes
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.feature.auth.viewmodel.AuthViewModel

/**
 * Login screen.
 *
 * Provides email/password input, password visibility toggle,
 * login button, error display, and progress indicator.
 *
 * @param viewModel Authentication ViewModel
 * @param onLoggedIn Callback invoked upon successful login
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    // Navigate on successful login (only once)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is AuthViewModel.UiEvent.LoggedIn) {
                onLoggedIn()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.ScreenLarge),
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(stringResource(R.string.login), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(Spacing.ScreenLarge))

            // Email input field
            OutlinedTextField(
                value = ui.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(R.string.mail)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.GapSm))

            // Password input field (with visibility toggle icon)
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

            // Error display
            if (ui.error != null) {
                Spacer(Modifier.height(Spacing.GapSm))
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(Spacing.GapMd))

            // Login button (with progress indicator)
            Button(
                onClick = viewModel::login,
                enabled = !ui.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.isLoading) CircularProgressIndicator(
                    strokeWidth = Sizes.Border,
                    modifier = Modifier.size(Sizes.IconSm)
                )
                else Text(stringResource(R.string.login))
            }
        }
    }
}