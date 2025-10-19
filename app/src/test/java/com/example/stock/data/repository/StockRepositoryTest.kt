package com.example.stock.data.repository

import com.example.stock.data.model.SymbolItem
import com.example.stock.data.network.CandleDto
import com.example.stock.data.network.StockApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var stockApi: StockApi
    private lateinit var repo: StockRepository

    @Before
    fun setup() {
        stockApi = mockk()
        repo = StockRepository(stockApi = stockApi, io = dispatcher)
    }

    @Test
    fun `fetchSymbols updates symbols flow with API data`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)

        Dispatchers.setMain(dispatcher)

        val repo = StockRepository(stockApi = stockApi, io = dispatcher)

        val mockSymbols = listOf(
            SymbolItem(code = "AAPL", name = "Apple Inc."),
            SymbolItem(code = "GOOG", name = "Alphabet Inc.")
        )
        coEvery { stockApi.getSymbols() } returns mockSymbols

        repo.fetchSymbols()
        advanceUntilIdle()

        coVerify(exactly = 1) { stockApi.getSymbols() }
        assertEquals(mockSymbols, repo.symbols.value)

        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCandles updates candles flow with API data`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        val repo = StockRepository(stockApi = stockApi, io = dispatcher)

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

        Dispatchers.resetMain()
    }

    @Test
    fun `clearCandles empties the candles flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        val repo = StockRepository(stockApi = stockApi, io = dispatcher)

        // 事前に値をセット
        val initial = listOf(CandleDto("2024-01-01", 100.00, 110.00, 90.00, 105.00, 1000))
        coEvery { stockApi.getCandles(any(), any(), any()) } returns initial
        repo.fetchCandles("AAPL")
        advanceUntilIdle()

        // 実行
        repo.clearCandles()
        assertEquals(emptyList<CandleDto>(), repo.candles.value)

        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCandles uses default params`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        val repo = StockRepository(stockApi = stockApi, io = dispatcher)

        coEvery { stockApi.getCandles(any(), any(), any()) } returns emptyList()

        repo.fetchCandles("MSFT")
        advanceUntilIdle()

        coVerify(exactly = 1) { stockApi.getCandles("MSFT", "1day", 200) }

        Dispatchers.resetMain()
    }
}