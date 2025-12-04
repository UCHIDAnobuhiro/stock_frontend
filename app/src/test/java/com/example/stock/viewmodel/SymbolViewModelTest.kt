package com.example.stock.viewmodel

import com.example.stock.feature.stocklist.data.SymbolItem
import com.example.stock.feature.stocklist.data.StockRepository
import com.example.stock.feature.stocklist.viewmodel.SymbolViewModel
import com.example.stock.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SymbolViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repo: StockRepository
    private lateinit var vm: SymbolViewModel

    @Before
    fun setup() {
        repo = mockk()
        vm = SymbolViewModel(repo)
    }

    @Test
    fun `load success - updates symbols and clears error`() =
        runTest(mainRule.scheduler) {
            // given
            val expected = listOf(
                SymbolItem("AAPL", "Apple Inc."),
                SymbolItem("GOOG", "Alphabet Inc.")
            )
            coEvery { repo.fetchSymbols() } returns expected

            // when
            vm.load()

            // 完了まで進める
            advanceUntilIdle()

            // 成功状態を検証
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isNull()
            assertThat(state.symbols).isEqualTo(expected)
            coVerify(exactly = 1) { repo.fetchSymbols() }
        }

    @Test
    fun `load failure - sets error and keeps symbols unchanged`() =
        runTest(mainRule.scheduler) {
            // given
            coEvery { repo.fetchSymbols() } throws IOException("Network down")

            // when
            vm.load()

            // 完了まで進める
            advanceUntilIdle()

            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.symbols).isEqualTo(emptyList<SymbolItem>())
            assertThat(state.error).isEqualTo("Network down")

            coVerify(exactly = 1) { repo.fetchSymbols() }
        }

    @Test
    fun `load clears previous error on new request start`() = runTest(mainRule.scheduler) {
        // まず失敗させてエラー状態にする
        coEvery { repo.fetchSymbols() } throws IOException("first failure")
        vm.load()
        advanceUntilIdle()
        assertThat(vm.ui.value.error).isEqualTo("first failure")

        // 次は成功させる
        val expected = listOf(SymbolItem("MSFT", "Microsoft"))
        coEvery { repo.fetchSymbols() } returns expected

        vm.load()

        // リクエスト開始時に error がクリアされること
        runCurrent()
        assertThat(vm.ui.value.error).isNull()

        advanceUntilIdle()
        assertThat(vm.ui.value.symbols).isEqualTo(expected)
        assertThat(vm.ui.value.isLoading).isFalse()
    }

}