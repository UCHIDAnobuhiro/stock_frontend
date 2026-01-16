package com.example.stock.feature.stocklist.data.repository

import com.example.stock.feature.stocklist.data.remote.SymbolApi
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import com.example.stock.util.MainDispatcherRule
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
    private lateinit var repo: SymbolRepository

    @Before
    fun setup() {
        symbolApi = mockk()
        repo = SymbolRepository(symbolApi = symbolApi, io = mainRule.dispatcher)
    }

    @Test
    fun `fetchSymbols returns symbols from api`() = runTest(mainRule.scheduler) {
        // given
        val expected = listOf(
            SymbolDto("AAPL", "Apple Inc."),
            SymbolDto("GOOG", "Alphabet Inc.")
        )
        coEvery { symbolApi.getSymbols() } returns expected

        // when
        val result = repo.fetchSymbols()

        // then
        assertThat(result).isEqualTo(expected)
        coVerify(exactly = 1) { symbolApi.getSymbols() }
    }
}
