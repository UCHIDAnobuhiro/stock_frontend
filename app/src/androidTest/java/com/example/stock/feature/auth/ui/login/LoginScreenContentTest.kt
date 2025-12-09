package com.example.stock.feature.auth.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stock.R
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialState_displaysEmptyFields() {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        // Title "ログイン" (not clickable)
        composeTestRule.onNode(hasText("ログイン") and !hasClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText("メールアドレス").assertIsDisplayed()
        composeTestRule.onNodeWithText("パスワード").assertIsDisplayed()
    }

    @Test
    fun emailInput_callsOnEmailChange() {
        var capturedEmail = ""

        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChange = { capturedEmail = it },
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithText("メールアドレス").performTextInput("test@example.com")
        assertThat(capturedEmail).isEqualTo("test@example.com")
    }

    @Test
    fun passwordInput_callsOnPasswordChange() {
        var capturedPassword = ""

        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChange = {},
                onPasswordChange = { capturedPassword = it },
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithText("パスワード").performTextInput("secret123")
        assertThat(capturedPassword).isEqualTo("secret123")
    }

    @Test
    fun togglePasswordButton_callsOnTogglePassword() {
        var toggleCalled = false

        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(isPasswordVisible = false),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = { toggleCalled = true },
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("表示").performClick()
        assertThat(toggleCalled).isTrue()
    }

    @Test
    fun loginButton_callsOnLogin() {
        var loginCalled = false

        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = { loginCalled = true },
                onNavigateToSignup = {}
            )
        }

        // Login button (clickable) not the title
        composeTestRule.onNode(hasText("ログイン") and hasClickAction()).performClick()
        assertThat(loginCalled).isTrue()
    }

    @Test
    fun signupButton_callsOnNavigateToSignup() {
        var signupCalled = false

        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = { signupCalled = true }
            )
        }

        composeTestRule.onNodeWithText("アカウントをお持ちでない方はこちら").performClick()
        assertThat(signupCalled).isTrue()
    }

    @Test
    fun loadingState_disablesButtons() {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(isLoading = true),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        // Login button shows progress indicator, signup button is disabled
        composeTestRule.onNodeWithText("アカウントをお持ちでない方はこちら").assertIsNotEnabled()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(errorResId = R.string.error_invalid_email),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithText("メールアドレスの形式が正しくありません").assertIsDisplayed()
    }

    @Test
    fun passwordVisible_showsVisibilityIcon() {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(isPasswordVisible = true),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("隠す").assertIsDisplayed()
    }

    @Test
    fun passwordHidden_showsVisibilityOffIcon() {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(isPasswordVisible = false),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("表示").assertIsDisplayed()
    }

    @Test
    fun notLoadingState_enablesButtons() {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = LoginUiState(isLoading = false),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePassword = {},
                onLogin = {},
                onNavigateToSignup = {}
            )
        }

        composeTestRule.onNodeWithText("アカウントをお持ちでない方はこちら").assertIsEnabled()
    }
}
