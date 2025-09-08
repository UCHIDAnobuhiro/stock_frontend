package com.example.stock.viewmodel

import com.example.stock.data.model.LoginResponse
import com.example.stock.data.repository.AuthRepository
import com.example.stock.util.ResetMainDispatcherRule
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
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val resetMain = ResetMainDispatcherRule()

    private val testDispatcher get() = resetMain.dispatcher

    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        viewModel = AuthViewModel(repository)
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
    fun `onEmailChange updates state and clears error`() = runTest(testDispatcher) {
        // まずエラーを発生させる（空入力で login）
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        // 具体的なエラーメッセージを確認
        assertThat(viewModel.ui.value.error)
            .isEqualTo("メールアドレスとパスワードを入力してください")

        // onEmailChangeで修正 → エラーがクリアされることを確認
        viewModel.onEmailChange("test@example.com")

        assertThat(viewModel.ui.value.email).isEqualTo("test@example.com")
        assertThat(viewModel.ui.value.error).isNull()
    }


    @Test
    fun `onPasswordChange updates state and clears error`() = runTest(testDispatcher) {
        // まずエラーを発生させる（空入力で login）
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.login()

        // 具体的なエラーメッセージを確認
        assertThat(viewModel.ui.value.error)
            .isEqualTo("メールアドレスとパスワードを入力してください")

        // onEmailChangeで修正 → エラーがクリアされることを確認
        viewModel.onPasswordChange("secret123")

        assertThat(viewModel.ui.value.password).isEqualTo("secret123")
        assertThat(viewModel.ui.value.error).isNull()
    }

    @Test
    fun `togglePassword flips flag`() = runTest(testDispatcher) {
        val before = viewModel.ui.value.isPasswordVisible
        viewModel.togglePassword()
        assertThat(viewModel.ui.value.isPasswordVisible).isEqualTo(!before)
    }

    @Test
    fun `login blocks when already loading`() = runTest(testDispatcher) {
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
        runTest(testDispatcher) {
            viewModel.onEmailChange("")
            viewModel.onPasswordChange("")
            viewModel.login()

            assertThat(viewModel.ui.value.error).isNotEmpty()
            coVerify(exactly = 0) { repository.login(any(), any()) }
        }

    @Test
    fun `login validation - bad email blocks`() = runTest(testDispatcher) {
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password")
        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.error).isEqualTo("メールアドレスの形式が不正です")
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `login success - emits LoggedIn event and clears loading`() = runTest(testDispatcher) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery {
            repository.login(
                "test@example.com",
                "password"
            )
        } returns LoginResponse("token123")

        // イベントを待ち受け
        var received: AuthViewModel.UiEvent? = null
        val job: Job = launch {
            received = viewModel.events.first()
        }

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.error).isNull()
        assertThat(received).isEqualTo(AuthViewModel.UiEvent.LoggedIn)

        job.cancelAndJoin()

        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - http 401 maps to credential message`() = runTest(testDispatcher) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws httpError(401)

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.isLoading).isFalse()
        assertThat(viewModel.ui.value.error).isEqualTo("メールアドレスまたはパスワードが間違っています")
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - http 500 shows http code`() = runTest(testDispatcher) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws httpError(500)

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.error).isEqualTo("HTTPエラー: 500")
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - io maps to network message`() = runTest(testDispatcher) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws IOException("timeout")

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.error).isEqualTo("通信エラー: ネットワークを確認してください")
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `login failure - serialization maps to json message`() = runTest(testDispatcher) {
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password")
        coEvery { repository.login(any(), any()) } throws SerializationException("bad json")

        viewModel.login()
        advanceUntilIdle()

        assertThat(viewModel.ui.value.error).isEqualTo("JSONエラー: レスポンス形式が不正です")
        coVerify(exactly = 1) { repository.login("test@example.com", "password") }
    }

    @Test
    fun `logout calls repository`() = runTest(testDispatcher) {
        viewModel.logout()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.logout() }
    }
}