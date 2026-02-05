package com.example.stock.feature.chart.viewmodel

import com.example.stock.R
import com.example.stock.core.util.DispatcherProvider
import com.example.stock.feature.chart.data.repository.CandleRepository
import com.example.stock.feature.chart.domain.model.Candle
import com.example.stock.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CandlesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repo: CandleRepository
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var vm: CandlesViewModel

    // ViewModel から見えるローソク足のストリームをテスト用に差し込み
    private lateinit var candlesFlow: MutableStateFlow<List<Candle>>

    @Before
    fun setup() {
        repo = mockk(relaxed = true)
        candlesFlow = MutableStateFlow(emptyList())

        // テスト用ディスパッチャを設定
        val testDispatcher = StandardTestDispatcher(mainRule.scheduler)
        dispatcherProvider = mockk {
            every { main } returns testDispatcher
            every { io } returns testDispatcher
            every { default } returns testDispatcher
        }

        // repository のプロパティ getter を差し替え
        every { repo.candles } returns candlesFlow
        coEvery { repo.fetchCandles(any(), any(), any()) } just Runs
        every { repo.clearCandles() } just Runs

        vm = CandlesViewModel(repo, dispatcherProvider)
    }

    @Test
    fun `load success puts transformed items into ui and clears loading`() =
        runTest(mainRule.scheduler) {
            // given
            val c1 = Candle("t1", 1.0, 2.0, 0.5, 1.5, 100)
            val c2 = Candle("t2", 1.2, 2.2, 0.7, 1.7, 120)
            candlesFlow.value = listOf(c1, c2)

            // fetch を明示的にブロックする
            val gate = CompletableDeferred<Unit>()
            coEvery { repo.fetchCandles(any(), any(), any()) } coAnswers {
                gate.await() // ここで待機 → isLoadingの検証が可能になる
            }

            // when
            vm.load("AAPL")

            // launch が走って isLoading が true になるまで現在キューを消化
            runCurrent()
            assertThat(vm.ui.value.isLoading).isTrue()

            // fetch 完了を解放
            gate.complete(Unit)
            // 完了まで進める
            advanceUntilIdle()

            // then
            val ui = vm.ui.value
            assertThat(ui.isLoading).isFalse()
            assertThat(ui.errorResId).isNull()
            assertThat(ui.items).hasSize(2)
            assertThat(ui.items[0].time).isEqualTo("t1")
            assertThat(ui.items[0].close).isWithin(0.0001).of(1.5)
            assertThat(ui.items[1].volume).isEqualTo(120L)
        }

    @Test
    fun `load sets error when repository throws IOException`() =
        runTest(mainRule.scheduler) {
            // given
            coEvery { repo.fetchCandles(any(), any(), any()) } throws IOException("network")

            // when
            vm.load("AAPL")
            advanceUntilIdle()

            // then
            val ui = vm.ui.value
            assertThat(ui.isLoading).isFalse()
            assertThat(ui.errorResId).isEqualTo(R.string.error_network)
            assertThat(ui.items).isEmpty()
        }

    @Test
    fun `load with blank code sets validation error and does not call repo`() =
        runTest(mainRule.scheduler) {
            // when
            vm.load("")

            // then
            runCurrent()
            val ui = vm.ui.value
            assertThat(ui.isLoading).isFalse()
            assertThat(ui.errorResId).isEqualTo(R.string.error_empty_stock_code)
            coVerify(exactly = 0) { repo.fetchCandles(any(), any(), any()) }
        }

    @Test
    fun `load calls fetchCandles with defaults`() = runTest(mainRule.scheduler) {
        vm.load(code = "AAPL")
        advanceUntilIdle()
        coVerify(exactly = 1) { repo.fetchCandles("AAPL", "1day", 200) }
    }

    @Test
    fun `load calls fetchCandles with explicit params`() = runTest(mainRule.scheduler) {
        vm.load(code = "GOOG", interval = "1h", outputsize = 500)
        advanceUntilIdle()
        coVerify(exactly = 1) { repo.fetchCandles("GOOG", "1h", 500) }
    }

    @Test
    fun `clear cancels fetching, clears repo and resets ui`() = runTest(mainRule.scheduler) {
        vm.clear()
        verify(exactly = 1) { repo.clearCandles() }
        val ui = vm.ui.value
        assertThat(ui.isLoading).isFalse()
        assertThat(ui.items).isEmpty()
        assertThat(ui.errorResId).isNull()
    }
}
