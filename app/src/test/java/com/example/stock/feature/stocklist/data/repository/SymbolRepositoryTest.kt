package com.example.stock.feature.stocklist.data.repository

import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SymbolRepositoryTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    private lateinit var symbolApi: SymbolApi
    private lateinit var repo: SymbolRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        symbolApi = mockk()
        repo = SymbolRepository(symbolApi = symbolApi, io = dispatcher)
    }

    @Test
    fun `fetchSymbols should return symbols from api`() = runTest(scheduler) {
        val expected = listOf(
            SymbolItem("AAPL", "Apple Inc."),
            SymbolItem("GOOG", "Alphabet Inc.")
        )
        coEvery { symbolApi.getSymbols() } returns expected

        // when
        val result = repo.fetchSymbols()

        // then
        assertEquals(expected, result)
        coVerify(exactly = 1) { symbolApi.getSymbols() }
    }
}
