package com.example.stock.viewmodel

import com.example.stock.R
import com.example.stock.feature.auth.data.remote.LoginResponse
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.viewmodel.LoginViewModel
import com.example.stock.util.MainDispatcherRule
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
class LoginViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        viewModel = LoginViewModel(repository)
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
        // まずエラーを発生させる（空入力で login）
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        // エラーが設定されていることを確認
        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // onEmailChangeで修正 → エラーがクリアされることを確認
        viewModel.onEmailChange("test@example.com")

        assertThat(viewModel.ui.value.email).isEqualTo("test@example.com")
        assertThat(viewModel.ui.value.errorResId).isNull()
    }


    @Test
    fun `onPasswordChange updates state and clears error`() = runTest(mainRule.scheduler) {
        // まずエラーを発生させる（空入力で login）
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        // エラーが設定されていることを確認
        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // onPasswordChangeで修正 → エラーがクリアされることを確認
        viewModel.onPasswordChange("secret123")

        assertThat(viewModel.ui.value.password).isEqualTo("secret123")
        assertThat(viewModel.ui.value.errorResId).isNull()
    }

    @Test
    fun `togglePassword flips flag`() = runTest(mainRule.scheduler) {
        val before = viewModel.ui.value.isPasswordVisible
        viewModel.togglePassword()
        assertThat(viewModel.ui.value.isPasswordVisible).isEqualTo(!before)
    }

    @Test
    fun `login blocks when already loading`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")

        val gate = Channel<Unit>(capacity = 0)
        coEvery { repository.login(any(), any()) } coAnswers {
            gate.receive()
            LoginResponse("tkn")
        }

        // 1回目の login を開始
        backgroundScope.launch { viewModel.login() }

        // isLoading が true になるまで待つ（ビューの状態で同期を取る）
        withTimeout(1_000) {
            viewModel.ui.first { it.isLoading }
        }

        // 2回目の login を開始
        backgroundScope.launch { viewModel.login() }

        // 1回目を完了させる
        gate.trySend(Unit)

        advanceUntilIdle()

        // repo.login は最終的に1回だけ
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login validation - blank fields sets error and does not call repository`() =
        runTest(mainRule.scheduler) {
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("")
            viewModel.login()

            assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_empty_fields)
            coVerify(exactly = 0) { repository.login(any(), any()) }
        }

    @Test
    fun `login validation - bad email blocks`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password")
        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_invalid_email)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `login validation - short password blocks`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("short")
        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_password_too_short)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `login success - emits LoggedIn event and clears loading`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery {
            repository.login(
                "test@example.com",
                "password"
            )
        } returns LoginResponse("token123")

        // イベントを待ち受け
        var received: LoginViewModel.UiEvent? = null
        val job: Job = launch {
            received = viewModel.events.first()
        }

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.errorResId).isNull()
        assertThat(received).isEqualTo(LoginViewModel.UiEvent.LoggedIn)

        job.cancelAndJoin()

        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - sets generic error message`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws httpError(401)

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_login_failed)
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - network error sets generic error message`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws IOException("timeout")

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_login_failed)
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `logout calls repository and emits LoggedOut event`() = runTest(mainRule.scheduler) {
        var received: LoginViewModel.UiEvent? = null
        val job: Job = launch {
            received = viewModel.events.first()
        }

        viewModel.logout()
        advanceUntilIdle()

        assertThat(received).isEqualTo(LoginViewModel.UiEvent.LoggedOut)
        job.cancelAndJoin()

        coVerify(exactly = 1) { repository.logout() }
    }

    @Test
    fun `logout resets all UI state to initial values`() = runTest(mainRule.scheduler) {
        // Set up non-default state
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.togglePassword()
        viewModel.login() // This will set an error since repo is mocked
        advanceUntilIdle()

        // Verify state is modified
        assertThat(viewModel.ui.value.email).isNotEmpty()
        assertThat(viewModel.ui.value.password).isNotEmpty()
        assertThat(viewModel.ui.value.isPasswordVisible).isTrue()

        // Logout
        viewModel.logout()
        advanceUntilIdle()

        // Verify all state is reset to initial values
        val state = viewModel.ui.value
        assertThat(state.email).isEmpty()
        assertThat(state.password).isEmpty()
        assertThat(state.isPasswordVisible).isFalse()
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorResId).isNull()

        coVerify(exactly = 1) { repository.login("test@example.com", "password123") }
        coVerify(exactly = 1) { repository.logout() }
    }
}
