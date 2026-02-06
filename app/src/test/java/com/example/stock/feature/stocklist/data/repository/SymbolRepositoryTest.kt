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
import java.io.IOException

/**
 * [SymbolRepository]のユニットテスト。
 *
 * リポジトリがAPIから銘柄を正しく取得することを検証。
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
        // 準備
        val dtos = listOf(
            SymbolDto("AAPL", "Apple Inc."),
            SymbolDto("GOOG", "Alphabet Inc.")
        )
        val expected = listOf(
            Symbol("AAPL", "Apple Inc."),
            Symbol("GOOG", "Alphabet Inc.")
        )
        coEvery { symbolApi.getSymbols() } returns dtos

        // 実行
        val result = repo.fetchSymbols()

        // 検証
        assertThat(result).isEqualTo(expected)
        coVerify(exactly = 1) { symbolApi.getSymbols() }
    }

    @Test
    fun `fetchSymbols returns empty list when api returns empty`() = runTest(mainRule.scheduler) {
        // 準備
        coEvery { symbolApi.getSymbols() } returns emptyList()

        // 実行
        val result = repo.fetchSymbols()

        // 検証
        assertThat(result).isEmpty()
    }

    @Test
    fun `fetchSymbols propagates api exception`() = runTest(mainRule.scheduler) {
        // 準備
        coEvery { symbolApi.getSymbols() } throws IOException("Network error")

        // 実行 / 検証
        val result = runCatching { repo.fetchSymbols() }
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        assertThat(result.exceptionOrNull()).hasMessageThat().isEqualTo("Network error")
    }
}
