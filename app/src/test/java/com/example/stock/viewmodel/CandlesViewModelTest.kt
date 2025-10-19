package com.example.stock.viewmodel

import com.example.stock.data.network.CandleDto
import com.example.stock.data.repository.StockRepository
import com.example.stock.util.ResetMainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CandlesViewModelTest {

    @get:Rule
    val resetMainRule = ResetMainDispatcherRule()

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

        vm = CandlesViewModel(repo)
    }

    @Test
    fun `candles is passthrough of repository flow`() = runTest {
        // ダミー CandleDto（フィールド不明なため relaxed mock を使用）
        val c1 = mockk<CandleDto>(relaxed = true)
        val c2 = mockk<CandleDto>(relaxed = true)

        // repo 側の Flow を更新 → ViewModel 側に反映されるはず
        candlesFlow.value = listOf(c1)
        assertEquals(listOf(c1), vm.candles.value)

        candlesFlow.value = listOf(c1, c2)
        assertEquals(listOf(c1, c2), vm.candles.value)
    }

    @Test
    fun `load calls fetchCandles with defaults`() = runTest {
        // fetchCandles はサスペンド関数想定
        coEvery { repo.fetchCandles(any(), any(), any()) } returns Unit

        vm.load(code = "AAPL") // interval=1day, outputsize=200 のデフォルト

        advanceUntilIdle() // viewModelScope.launch の完了を待つ
        coVerify(exactly = 1) { repo.fetchCandles("AAPL", "1day", 200) }
    }

    @Test
    fun `load calls fetchCandles with explicit params`() = runTest {
        coEvery { repo.fetchCandles(any(), any(), any()) } returns Unit

        vm.load(code = "GOOG", interval = "1h", outputsize = 500)

        advanceUntilIdle()
        coVerify(exactly = 1) { repo.fetchCandles("GOOG", "1h", 500) }
    }

    @Test
    fun `clear calls repository clearCandles`() = runTest {
        vm.clear()
        verify(exactly = 1) { repo.clearCandles() }
    }
}