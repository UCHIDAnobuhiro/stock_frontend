package com.example.stock.feature.auth.viewmodel

import com.example.stock.R
import com.example.stock.feature.auth.data.remote.SignupResponse
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.signup.SignupUiEvent
import com.example.stock.util.MainDispatcherRule
import com.example.stock.util.TestDispatcherProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SignupViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var viewModel: SignupViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        dispatcherProvider = TestDispatcherProvider(mainRule.scheduler)
        viewModel = SignupViewModel(repository, dispatcherProvider)
    }

    @After
    fun tearDown() {
        confirmVerified(repository)
    }

    private fun httpError(code: Int): HttpException {
        val body = """{"message":"err"}""".toResponseBody("application/json".toMediaType())
        val resp: Response<Any> = Response.error(code, body)
        return HttpException(resp)
    }

    @Test
    fun `onEmailChange updates state and clears error`() = runTest(mainRule.scheduler) {
        // Trigger error state by attempting signup with empty fields
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onConfirmPasswordChange("")
        viewModel.signup()

        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // Changing email should clear the error
        viewModel.onEmailChange("test@example.com")

        assertThat(viewModel.ui.value.email).isEqualTo("test@example.com")
        assertThat(viewModel.ui.value.errorResId).isNull()
    }

    @Test
    fun `onPasswordChange updates state and clears error`() = runTest(mainRule.scheduler) {
        // Trigger error state by attempting signup with empty fields
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onConfirmPasswordChange("")
        viewModel.signup()

        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // Changing password should clear the error
        viewModel.onPasswordChange("secret123")

        assertThat(viewModel.ui.value.password).isEqualTo("secret123")
        assertThat(viewModel.ui.value.errorResId).isNull()
    }

    @Test
    fun `onConfirmPasswordChange updates state and clears error`() = runTest(mainRule.scheduler) {
        // Trigger error state by attempting signup with empty fields
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onConfirmPasswordChange("")
        viewModel.signup()

        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // Changing confirm password should clear the error
        viewModel.onConfirmPasswordChange("secret123")

        assertThat(viewModel.ui.value.confirmPassword).isEqualTo("secret123")
        assertThat(viewModel.ui.value.errorResId).isNull()
    }

    @Test
    fun `togglePassword flips flag`() = runTest(mainRule.scheduler) {
        val before = viewModel.ui.value.isPasswordVisible
        viewModel.togglePassword()
        assertThat(viewModel.ui.value.isPasswordVisible).isEqualTo(!before)
    }

    @Test
    fun `toggleConfirmPassword flips flag`() = runTest(mainRule.scheduler) {
        val before = viewModel.ui.value.isConfirmPasswordVisible
        viewModel.toggleConfirmPassword()
        assertThat(viewModel.ui.value.isConfirmPasswordVisible).isEqualTo(!before)
    }

    @Test
    fun `signup blocks when already loading`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password12345678")
        viewModel.onConfirmPasswordChange("password12345678")

        val gate = Channel<Unit>(capacity = 0)
        coEvery { repository.signup(any(), any()) } coAnswers {
            gate.receive()
            SignupResponse("User registered successfully")
        }

        // Start first signup
        backgroundScope.launch { viewModel.signup() }

        // Wait until loading state is true
        withTimeout(1_000) {
            viewModel.ui.first { it.isLoading }
        }

        // Attempt second signup while first is still in progress
        backgroundScope.launch { viewModel.signup() }

        // Complete the first signup
        gate.trySend(Unit)

        advanceUntilIdle()

        // Repository should only be called once
        coVerify(exactly = 1) { repository.signup("test@example.com", "password12345678") }
    }

    @Test
    fun `signup validation - blank fields sets error and does not call repository`() =
        runTest(mainRule.scheduler) {
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("")
            viewModel.onConfirmPasswordChange("")
            viewModel.signup()
            advanceUntilIdle()

            assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_empty_fields)
            coVerify(exactly = 0) { repository.signup(any(), any()) }
        }

    @Test
    fun `signup validation - bad email blocks`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_invalid_email)
        coVerify(exactly = 0) { repository.signup(any(), any()) }
    }

    @Test
    fun `signup validation - short password blocks`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("short")
        viewModel.onConfirmPasswordChange("short")
        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_password_too_short)
        coVerify(exactly = 0) { repository.signup(any(), any()) }
    }

    @Test
    fun `signup validation - password mismatch blocks`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("different123")
        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_passwords_do_not_match)
        coVerify(exactly = 0) { repository.signup(any(), any()) }
    }

    @Test
    fun `signup success - emits SignedUp event and clears loading`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        coEvery { repository.signup("test@example.com", "password123") } returns SignupResponse("User registered successfully")

        // Collect events before triggering signup
        var received: SignupUiEvent? = null
        val job: Job = launch {
            received = viewModel.events.first()
        }

        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.errorResId).isNull()
        assertThat(received).isEqualTo(SignupUiEvent.SignedUp)

        job.cancelAndJoin()

        coVerify(exactly = 1) { repository.signup("test@example.com", "password123") }
    }

    @Test
    fun `signup failure - http 409 maps to already registered message`() =
        runTest(mainRule.scheduler) {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onConfirmPasswordChange("password123")
            coEvery { repository.signup(any(), any()) } throws httpError(409)

            viewModel.signup()
            advanceUntilIdle()

            assertThat(viewModel.ui.value.isLoading).isFalse()
            assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_email_already_registered)
            coVerify(exactly = 1) { repository.signup("test@example.com", "password123") }
        }

    @Test
    fun `signup failure - http 500 shows generic error`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        coEvery { repository.signup(any(), any()) } throws httpError(500)

        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_signup_failed)
        coVerify(exactly = 1) { repository.signup("test@example.com", "password123") }
    }

    @Test
    fun `signup failure - io maps to generic error`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        coEvery { repository.signup(any(), any()) } throws IOException("timeout")

        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_signup_failed)
        coVerify(exactly = 1) { repository.signup("test@example.com", "password123") }
    }

    @Test
    fun `signup failure - serialization maps to generic error`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password123")
        coEvery { repository.signup(any(), any()) } throws SerializationException("bad json")

        viewModel.signup()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_signup_failed)
        coVerify(exactly = 1) { repository.signup("test@example.com", "password123") }
    }
}
