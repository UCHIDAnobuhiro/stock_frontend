package com.example.stock.data.repository

import com.example.stock.feature.stocklist.data.SymbolItem
import com.example.stock.feature.stocklist.data.CandleDto
import com.example.stock.feature.stocklist.data.StockApi
import com.example.stock.feature.stocklist.data.StockRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    private lateinit var stockApi: StockApi
    private lateinit var repo: StockRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        stockApi = mockk()
        repo = StockRepository(stockApi = stockApi, io = dispatcher)
    }

    @Test
    fun `fetchSymbols should return symbols from api`() = runTest(scheduler) {
        val expected = listOf(
            SymbolItem("AAPL", "Apple Inc."),
            SymbolItem("GOOG", "Alphabet Inc.")
        )
        coEvery { stockApi.getSymbols() } returns expected

        // when
        val result = repo.fetchSymbols()

        // then
        assertEquals(expected, result)
        coVerify(exactly = 1) { stockApi.getSymbols() }
    }

    @Test
    fun `fetchCandles updates candles flow with API data`() = runTest(scheduler) {
        val mockCandles = listOf(
            CandleDto(
                time = "2024-01-01",
                open = 100.00,
                high = 110.00,
                low = 90.00,
                close = 105.00,
                volume = 1000
            ),
            CandleDto(
                time = "2024-01-02",
                open = 105.00,
                high = 120.00,
                low = 100.00,
                close = 115.00,
                volume = 1500
            )
        )
        coEvery { stockApi.getCandles(any(), any(), any()) } returns mockCandles

        repo.fetchCandles("AAPL", "1day", 200)
        advanceUntilIdle()

        coVerify(exactly = 1) { stockApi.getCandles("AAPL", "1day", 200) }
        assertEquals(mockCandles, repo.candles.value)

    }

    @Test
    fun `clearCandles empties the candles flow`() = runTest(scheduler) {
        // 事前に値をセット
        val initial = listOf(CandleDto("2024-01-01", 100.00, 110.00, 90.00, 105.00, 1000))
        coEvery { stockApi.getCandles(any(), any(), any()) } returns initial
        repo.fetchCandles("AAPL")
        advanceUntilIdle()

        // 実行
        repo.clearCandles()
        assertEquals(emptyList<CandleDto>(), repo.candles.value)
    }

    @Test
    fun `fetchCandles uses default params`() = runTest(scheduler) {
        coEvery { stockApi.getCandles(any(), any(), any()) } returns emptyList()

        repo.fetchCandles("MSFT")
        advanceUntilIdle()

        coVerify(exactly = 1) { stockApi.getCandles("MSFT", "1day", 200) }
    }
}