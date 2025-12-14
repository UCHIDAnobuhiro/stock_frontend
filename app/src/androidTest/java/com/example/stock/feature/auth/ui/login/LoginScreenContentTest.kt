package com.example.stock.feature.auth.ui.login

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
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

    // Helper function to reduce boilerplate when setting up LoginScreenContent
    private fun setLoginScreen(
        uiState: LoginUiState = LoginUiState(),
        onEmailChange: (String) -> Unit = {},
        onPasswordChange: (String) -> Unit = {},
        onTogglePassword: () -> Unit = {},
        onLogin: () -> Unit = {},
        onNavigateToSignup: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LoginScreenContent(
                uiState = uiState,
                onEmailChange = onEmailChange,
                onPasswordChange = onPasswordChange,
                onTogglePassword = onTogglePassword,
                onLogin = onLogin,
                onNavigateToSignup = onNavigateToSignup
            )
        }
    }

    @Test
    fun initialState_displaysEmptyFields() {
        setLoginScreen()

        // Title "ログイン" (not clickable)
        composeTestRule.onNode(hasText("ログイン") and !hasClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText("メールアドレス").assertIsDisplayed()
        composeTestRule.onNodeWithText("パスワード").assertIsDisplayed()
    }

    @Test
    fun existingEmail_displaysInTextField() {
        setLoginScreen(uiState = LoginUiState(email = "existing@example.com"))

        // Email value from UiState is displayed in the text field
        composeTestRule.onNodeWithText("existing@example.com").assertIsDisplayed()
    }

    @Test
    fun existingPassword_displaysInTextField() {
        setLoginScreen(uiState = LoginUiState(password = "secret123", isPasswordVisible = true))

        // Password value from UiState is displayed when visible
        composeTestRule.onNodeWithText("secret123").assertIsDisplayed()
    }

    @Test
    fun emailInput_callsOnEmailChange() {
        var capturedEmail = ""
        setLoginScreen(onEmailChange = { capturedEmail = it })

        composeTestRule.onNodeWithText("メールアドレス").performTextInput("test@example.com")
        assertThat(capturedEmail).isEqualTo("test@example.com")
    }

    @Test
    fun passwordInput_callsOnPasswordChange() {
        var capturedPassword = ""
        setLoginScreen(onPasswordChange = { capturedPassword = it })

        composeTestRule.onNodeWithText("パスワード").performTextInput("secret123")
        assertThat(capturedPassword).isEqualTo("secret123")
    }

    @Test
    fun togglePasswordButton_callsOnTogglePassword() {
        var toggleCalled = false
        setLoginScreen(
            uiState = LoginUiState(isPasswordVisible = false),
            onTogglePassword = { toggleCalled = true }
        )

        composeTestRule.onNodeWithContentDescription("表示").performClick()
        assertThat(toggleCalled).isTrue()
    }

    @Test
    fun loginButton_callsOnLogin() {
        var loginCalled = false
        setLoginScreen(onLogin = { loginCalled = true })

        // Login button (clickable) not the title
        composeTestRule.onNode(hasText("ログイン") and hasClickAction()).performClick()
        assertThat(loginCalled).isTrue()
    }

    @Test
    fun signupButton_callsOnNavigateToSignup() {
        var signupCalled = false
        setLoginScreen(onNavigateToSignup = { signupCalled = true })

        composeTestRule.onNodeWithText("アカウントをお持ちでない方はこちら").performClick()
        assertThat(signupCalled).isTrue()
    }

    @Test
    fun loadingState_disablesSignupButton() {
        setLoginScreen(uiState = LoginUiState(isLoading = true))

        // Signup button is disabled during loading
        composeTestRule.onNodeWithText("アカウントをお持ちでない方はこちら").assertIsNotEnabled()
    }

    @Test
    fun loadingState_showsProgressIndicator() {
        setLoginScreen(uiState = LoginUiState(isLoading = true))

        // CircularProgressIndicator is displayed instead of login button text
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun notLoadingState_showsLoginButtonText() {
        setLoginScreen(uiState = LoginUiState(isLoading = false))

        // Login button text is displayed when not loading
        composeTestRule.onNode(hasText("ログイン") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        setLoginScreen(uiState = LoginUiState(errorResId = R.string.error_invalid_email))

        composeTestRule.onNodeWithText("メールアドレスの形式が正しくありません").assertIsDisplayed()
    }

    @Test
    fun passwordVisible_showsVisibilityIcon() {
        setLoginScreen(uiState = LoginUiState(isPasswordVisible = true))

        composeTestRule.onNodeWithContentDescription("隠す").assertIsDisplayed()
    }

    @Test
    fun passwordHidden_showsVisibilityOffIcon() {
        setLoginScreen(uiState = LoginUiState(isPasswordVisible = false))

        composeTestRule.onNodeWithContentDescription("表示").assertIsDisplayed()
    }

    @Test
    fun notLoadingState_enablesButtons() {
        setLoginScreen(uiState = LoginUiState(isLoading = false))

        composeTestRule.onNodeWithText("アカウントをお持ちでない方はこちら").assertIsEnabled()
    }
}
