package com.example.stock.feature.auth.ui.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stock.R
import com.example.stock.core.ui.theme.Sizes
import com.example.stock.core.ui.theme.Spacing
import com.example.stock.feature.auth.viewmodel.SignupViewModel

/**
 * Signup screen with ViewModel.
 *
 * Wrapper composable that connects [SignupViewModel] to [SignupScreenContent].
 * Handles event collection and state observation.
 * Uses Hilt to automatically inject the SignupViewModel.
 *
 * @param onSignedUp Callback invoked upon successful signup
 * @param onNavigateToLogin Callback to navigate to login screen
 * @param viewModel Signup ViewModel (injected by Hilt)
 */
@Composable
fun SignupScreen(
    onSignedUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    // Navigate on successful signup (only once)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is SignupUiEvent.SignedUp) {
                onSignedUp()
            }
        }
    }

    SignupScreenContent(
        uiState = ui,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePassword = viewModel::togglePassword,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onToggleConfirmPassword = viewModel::toggleConfirmPassword,
        onSignup = viewModel::signup,
        onNavigateToLogin = onNavigateToLogin
    )
}

/**
 * Stateless signup screen content.
 *
 * Provides email/password/confirm password input, password visibility toggles,
 * signup button, error display, and progress indicator.
 *
 * @param uiState Current UI state
 * @param onEmailChange Callback when email input changes
 * @param onPasswordChange Callback when password input changes
 * @param onTogglePassword Callback to toggle password visibility
 * @param onConfirmPasswordChange Callback when confirm password input changes
 * @param onToggleConfirmPassword Callback to toggle confirm password visibility
 * @param onSignup Callback to execute signup
 * @param onNavigateToLogin Callback to navigate to login screen
 */
@Composable
fun SignupScreenContent(
    uiState: SignupUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onSignup: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.mail)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.GapSm))

            // Password input field (with visibility toggle icon)
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ),
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = stringResource(
                                if (uiState.isPasswordVisible) R.string.hide else R.string.show
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester)
            )
            Spacer(Modifier.height(Spacing.GapSm))

            // Confirm Password input field (with visibility toggle icon)
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(stringResource(R.string.confirm_password)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (!uiState.isLoading) onSignup()
                    }
                ),
                visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleConfirmPassword) {
                        Icon(
                            imageVector = if (uiState.isConfirmPasswordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = stringResource(
                                if (uiState.isConfirmPasswordVisible) R.string.hide else R.string.show
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester)
            )

            // Error display
            uiState.errorResId?.let { errorResId ->
                Spacer(Modifier.height(Spacing.GapSm))
                Text(stringResource(errorResId), color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(Spacing.GapMd))

            // Signup button (with progress indicator)
            Button(
                onClick = onSignup,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(
                    strokeWidth = Sizes.Border,
                    modifier = Modifier.size(Sizes.IconSm)
                )
                else Text(stringResource(R.string.signup))
            }

            Spacer(Modifier.height(Spacing.GapSm))

            // Navigate to login
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.already_have_account))
            }
        }
    }
}
