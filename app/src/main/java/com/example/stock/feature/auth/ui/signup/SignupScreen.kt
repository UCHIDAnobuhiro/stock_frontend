package com.example.stock.feature.auth.ui.signup

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stock.R
import com.example.stock.core.ui.theme.Sizes
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.feature.auth.viewmodel.SignupViewModel

/**
 * Signup screen.
 *
 * Provides email/password/confirm password input, password visibility toggle,
 * signup button, error display, and progress indicator.
 *
 * @param viewModel Signup ViewModel
 * @param onSignedUp Callback invoked upon successful signup
 * @param onNavigateToLogin Callback to navigate to login screen
 */
@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignedUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    // Navigate on successful signup (only once)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is SignupViewModel.UiEvent.SignedUp) {
                onSignedUp()
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
            Text(stringResource(R.string.signup), style = MaterialTheme.typography.headlineMedium)
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
            Spacer(Modifier.height(Spacing.GapSm))

            // Confirm Password input field (with visibility toggle icon)
            OutlinedTextField(
                value = ui.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text(stringResource(R.string.confirm_password)) },
                singleLine = true,
                visualTransformation = if (ui.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = viewModel::toggleConfirmPassword) {
                        Icon(
                            imageVector = if (ui.isConfirmPasswordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (ui.isConfirmPasswordVisible) stringResource(R.string.hide) else stringResource(
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

            // Signup button (with progress indicator)
            Button(
                onClick = viewModel::signup,
                enabled = !ui.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.isLoading) CircularProgressIndicator(
                    strokeWidth = Sizes.Border,
                    modifier = Modifier.size(Sizes.IconSm)
                )
                else Text(stringResource(R.string.signup))
            }

            Spacer(Modifier.height(Spacing.GapSm))

            // Navigate to login
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.already_have_account))
            }
        }
    }
}
