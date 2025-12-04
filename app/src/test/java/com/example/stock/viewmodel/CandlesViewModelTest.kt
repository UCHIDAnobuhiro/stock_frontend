package com.example.stock.viewmodel

import com.example.stock.feature.stocklist.data.CandleDto
import com.example.stock.feature.stocklist.data.StockRepository
import com.example.stock.feature.chart.viewmodel.CandlesViewModel
import com.example.stock.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

    private lateinit var repo: StockRepository
    private lateinit var vm: CandlesViewModel

    // ViewModel から見えるローソク足のストリームをテスト用に差し込み
    private lateinit var candlesFlow: MutableStateFlow<List<CandleDto>>

    @Before
    fun setup() {
        repo = mockk(relaxed = true)
        candlesFlow = MutableStateFlow(emptyList())

        // repository のプロパティ getter を差し替え
        every { repo.candles } returns candlesFlow
        coEvery { repo.fetchCandles(any(), any(), any()) } just Runs
        every { repo.clearCandles() } just Runs

        vm = CandlesViewModel(repo)
    }

    @Test
    fun `load success puts transformed items into ui and clears loading`() =
        runTest(mainRule.scheduler) {
            // given
            val c1 = CandleDto("t1", 1.0, 2.0, 0.5, 1.5, 100)
            val c2 = CandleDto("t2", 1.2, 2.2, 0.7, 1.7, 120)
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
            assertTrue(vm.ui.value.isLoading)   // ← ここが安定して true になる

            // fetch 完了を解放
            gate.complete(Unit)
            // 完了まで進める
            advanceUntilIdle()

            // then
            val ui = vm.ui.value
            assertFalse(ui.isLoading)
            assertNull(ui.error)
            assertEquals(2, ui.items.size)
            assertEquals("t1", ui.items[0].time)
            assertEquals(1.5, ui.items[0].close, 0.0001)
            assertEquals(120L, ui.items[1].volume)
        }

    @Test
    fun `load sets error when repository throws and clears loading`() =
        runTest(mainRule.scheduler) {
            // given
            coEvery { repo.fetchCandles(any(), any(), any()) } throws IOException(
                "network"
            )

            // when
            vm.load("AAPL")
            advanceUntilIdle()

            // then
            val ui = vm.ui.value
            assertFalse(ui.isLoading)
            assertNotNull(ui.error)
            assertTrue(ui.error!!.contains("通信エラー"))
            assertTrue(ui.items.isEmpty())
        }

    @Test
    fun `load with blank code sets validation error and does not call repo`() =
        runTest(mainRule.scheduler) {
            // when
            vm.load("")

            // then
            runCurrent()
            val ui = vm.ui.value
            assertFalse(ui.isLoading)
            assertNotNull(ui.error)
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
        assertFalse(ui.isLoading)
        assertTrue(ui.items.isEmpty())
        assertNull(ui.error)
    }

}