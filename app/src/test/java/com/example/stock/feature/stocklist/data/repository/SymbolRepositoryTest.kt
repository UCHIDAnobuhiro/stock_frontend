package com.example.stock.feature.stocklist.data.repository

import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import com.example.stock.feature.stocklist.domain.model.Symbol
import com.example.stock.util.MainDispatcherRule
import com.example.stock.util.TestDispatcherProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [SymbolRepository].
 *
 * Verifies that the repository correctly fetches symbols from the API.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SymbolRepositoryTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var symbolApi: SymbolApi
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var repo: SymbolRepository

    @Before
    fun setup() {
        symbolApi = mockk()
        dispatcherProvider = TestDispatcherProvider(mainRule.scheduler)
        repo = SymbolRepository(symbolApi = symbolApi, dispatcherProvider = dispatcherProvider)
    }

    @Test
    fun `fetchSymbols returns symbols from api`() = runTest(mainRule.scheduler) {
        // given
        val dtos = listOf(
            SymbolDto("AAPL", "Apple Inc."),
            SymbolDto("GOOG", "Alphabet Inc.")
        )
        val expected = listOf(
            Symbol("AAPL", "Apple Inc."),
            Symbol("GOOG", "Alphabet Inc.")
        )
        coEvery { symbolApi.getSymbols() } returns dtos

        // when
        val result = repo.fetchSymbols()

        // then
        assertThat(result).isEqualTo(expected)
        coVerify(exactly = 1) { symbolApi.getSymbols() }
    }
}
