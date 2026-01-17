package com.example.stock.feature.chart.data.repository

import com.example.stock.feature.chart.data.remote.CandleDto
import com.example.stock.feature.chart.data.remote.ChartApi
import com.example.stock.util.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CandleRepositoryTest {

    private val scheduler = TestCoroutineScheduler()
    private lateinit var dispatcherProvider: TestDispatcherProvider

    private lateinit var chartApi: ChartApi
    private lateinit var repo: CandleRepository

    @Before
    fun setup() {
        dispatcherProvider = TestDispatcherProvider(scheduler)
        Dispatchers.setMain(dispatcherProvider.main)
        chartApi = mockk()
        repo = CandleRepository(chartApi = chartApi, dispatcherProvider = dispatcherProvider)
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
        coEvery { chartApi.getCandles(any(), any(), any()) } returns mockCandles

        repo.fetchCandles("AAPL", "1day", 200)
        advanceUntilIdle()

        coVerify(exactly = 1) { chartApi.getCandles("AAPL", "1day", 200) }
        assertEquals(mockCandles, repo.candles.value)
    }

    @Test
    fun `clearCandles empties the candles flow`() = runTest(scheduler) {
        val initial = listOf(CandleDto("2024-01-01", 100.00, 110.00, 90.00, 105.00, 1000))
        coEvery { chartApi.getCandles(any(), any(), any()) } returns initial
        repo.fetchCandles("AAPL")
        advanceUntilIdle()

        repo.clearCandles()
        assertEquals(emptyList<CandleDto>(), repo.candles.value)
    }

    @Test
    fun `fetchCandles uses default params`() = runTest(scheduler) {
        coEvery { chartApi.getCandles(any(), any(), any()) } returns emptyList()

        repo.fetchCandles("MSFT")
        advanceUntilIdle()

        coVerify(exactly = 1) { chartApi.getCandles("MSFT", "1day", 200) }
    }
}
