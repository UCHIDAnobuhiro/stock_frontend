package com.example.stock.feature.auth.viewmodel

import com.example.stock.core.data.auth.AuthEventManager
import com.example.stock.feature.auth.data.repository.AuthRepository
import com.example.stock.util.MainDispatcherRule
import com.example.stock.util.TestDispatcherProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LogoutViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var authEventManager: AuthEventManager
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var viewModel: LogoutViewModel

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        authEventManager = AuthEventManager()
        dispatcherProvider = TestDispatcherProvider(mainRule.scheduler)
        viewModel = LogoutViewModel(repository, dispatcherProvider, authEventManager)
    }

    @After
    fun tearDown() {
        confirmVerified(repository)
    }

    @Test
    fun `logout success - calls repository and emits LoggedOut event`() =
        runTest(mainRule.scheduler) {
            // ログアウトをトリガーする前にイベントを収集
            var received: LogoutViewModel.UiEvent? = null
            val job: Job = launch {
                received = viewModel.events.first()
            }

            viewModel.logout()
            advanceUntilIdle()

            // LoggedOutイベントが発行されることを検証
            assertThat(received).isEqualTo(LogoutViewModel.UiEvent.LoggedOut)
            job.cancelAndJoin()

            coVerify(exactly = 1) { repository.logout() }
        }

    @Test
    fun `logout failure - emits LoggedOut event even when repository throws exception`() =
        runTest(mainRule.scheduler) {
            // ログアウト中のネットワークエラーをシミュレート
            coEvery { repository.logout() } throws IOException("Network error")

            // ログアウトをトリガーする前にイベントを収集
            var received: LogoutViewModel.UiEvent? = null
            val job: Job = launch {
                received = viewModel.events.first()
            }

            viewModel.logout()
            advanceUntilIdle()

            // 例外にもかかわらずLoggedOutイベントが発行されることを検証
            assertThat(received).isEqualTo(LogoutViewModel.UiEvent.LoggedOut)
            job.cancelAndJoin()

            coVerify(exactly = 1) { repository.logout() }
        }

    @Test
    fun `session expired event - emits LoggedOut event`() =
        runTest(mainRule.scheduler) {
            // セッション期限切れをトリガーする前にイベントを収集
            var received: LogoutViewModel.UiEvent? = null
            val job: Job = launch {
                received = viewModel.events.first()
            }

            // 401セッション期限切れをシミュレート
            authEventManager.emitSessionExpired()
            advanceUntilIdle()

            // LoggedOutイベントが発行されることを検証
            assertThat(received).isEqualTo(LogoutViewModel.UiEvent.LoggedOut)
            job.cancelAndJoin()
        }
}
