package com.example.stock.viewmodel

import com.example.stock.data.model.SymbolItem
import com.example.stock.data.repository.StockRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SymbolViewModelTest {

    private lateinit var repo: StockRepository
    private lateinit var vm: SymbolViewModel

    // Repositoryから差し込むテスト用のFlow
    private lateinit var symbolsFlow: MutableStateFlow<List<SymbolItem>>

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)
        symbolsFlow = MutableStateFlow(emptyList())
        every { repo.symbols } returns symbolsFlow
    }

    @After
    fun tearDown() {
        // 念のため毎テスト後にMainを元に戻す
        runCatching { Dispatchers.resetMain() }
    }

    @Test
    fun `symbols is passthrough of repository flow`() = runTest {
        // runTestのtestSchedulerを使ってMainを差し替え
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        vm = SymbolViewModel(repo)

        val s1 = listOf(SymbolItem(code = "AAPL", name = "Apple Inc."))
        val s2 = listOf(
            SymbolItem(code = "AAPL", name = "Apple Inc."),
            SymbolItem(code = "GOOG", name = "Alphabet Inc.")
        )

        symbolsFlow.value = s1
        assertEquals(s1, vm.symbols.value)

        symbolsFlow.value = s2
        assertEquals(s2, vm.symbols.value)
    }

    @Test
    fun `load triggers repository fetchSymbols`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)

        vm = SymbolViewModel(repo)

        coEvery { repo.fetchSymbols() } returns Unit

        vm.load()

        // viewModelScope.launch を完了させる
        advanceUntilIdle()

        coVerify(exactly = 1) { repo.fetchSymbols() }
    }
}