package com.example.stock.feature.auth.viewmodel

import com.example.stock.R
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.feature.auth.ui.login.LoginUiEvent
import com.example.stock.util.MainDispatcherRule
import com.example.stock.util.TestDispatcherProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
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
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        dispatcherProvider = TestDispatcherProvider(mainRule.scheduler)
        viewModel = LoginViewModel(repository, dispatcherProvider)
    }

    @After
    fun tearDown() {
        confirmVerified(repository)
    }

    private fun httpError(): HttpException {
        val body = """{"message":"err"}""".toResponseBody("application/json".toMediaType())
        val resp: Response<Any> = Response.error(401, body)
        return HttpException(resp)
    }

    @Test
    fun `onEmailChange updates state and clears error`() = runTest(mainRule.scheduler) {
        // 空のフィールドでログインを試みてエラー状態をトリガー
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // メールアドレスを変更するとエラーがクリアされること
        viewModel.onEmailChange("test@example.com")

        assertThat(viewModel.ui.value.email).isEqualTo("test@example.com")
        assertThat(viewModel.ui.value.errorResId).isNull()
    }

    @Test
    fun `onPasswordChange updates state and clears error`() = runTest(mainRule.scheduler) {
        // 空のフィールドでログインを試みてエラー状態をトリガー
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        assertThat(viewModel.ui.value.errorResId)
            .isEqualTo(R.string.error_empty_fields)

        // パスワードを変更するとエラーがクリアされること
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
        }

        // 最初のログインを開始
        backgroundScope.launch { viewModel.login() }

        // ローディング状態がtrueになるまで待機
        withTimeout(1_000) {
            viewModel.ui.first { it.isLoading }
        }

        // 最初のログインがまだ進行中に2回目のログインを試みる
        backgroundScope.launch { viewModel.login() }

        // 最初のログインを完了
        gate.trySend(Unit)

        advanceUntilIdle()

        // リポジトリは1回だけ呼ばれること
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login validation - blank fields sets error and does not call repository`() =
        runTest(mainRule.scheduler) {
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("")
            viewModel.login()
            advanceUntilIdle()

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
        coEvery { repository.login("test@example.com", "password") } returns Unit

        // ログインをトリガーする前にイベントを収集
        var received: LoginUiEvent? = null
        val job: Job = launch {
            received = viewModel.events.first()
        }

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.errorResId).isNull()
        assertThat(received).isEqualTo(LoginUiEvent.LoggedIn)

        job.cancelAndJoin()

        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - 401 error sets invalid credentials message`() = runTest(mainRule.scheduler) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws httpError()

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.errorResId).isEqualTo(R.string.error_invalid_credentials)
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
    fun `checkAuthState emits LoggedIn when token exists`() = runTest(mainRule.scheduler) {
        coEvery { repository.hasToken() } returns true

        var received: LoginUiEvent? = null
        val job: Job = launch {
            received = viewModel.events.first()
        }

        viewModel.checkAuthState()
        advanceUntilIdle()

        assertThat(received).isEqualTo(LoginUiEvent.LoggedIn)
        job.cancelAndJoin()

        coVerify(exactly = 1) { repository.hasToken() }
    }

    @Test
    fun `checkAuthState does not emit when no token exists`() = runTest(mainRule.scheduler) {
        coEvery { repository.hasToken() } returns false

        viewModel.checkAuthState()
        advanceUntilIdle()

        // タイムアウトを期待してイベントが発行されないことを検証
        val result = runCatching {
            withTimeout(100) {
                viewModel.events.first()
            }
        }
        assertThat(result.exceptionOrNull())
            .isInstanceOf(TimeoutCancellationException::class.java)

        coVerify(exactly = 1) { repository.hasToken() }
    }
}
