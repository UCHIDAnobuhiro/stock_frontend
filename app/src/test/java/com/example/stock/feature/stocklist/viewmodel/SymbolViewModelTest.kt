package com.example.stock.feature.stocklist.viewmodel

import com.example.stock.R
import com.example.stock.feature.stocklist.data.repository.SymbolRepository
import com.example.stock.feature.stocklist.domain.model.Symbol
import com.example.stock.feature.stocklist.ui.SymbolItem
import com.example.stock.util.MainDispatcherRule
import com.example.stock.util.TestDispatcherProvider
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
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SymbolViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repo: SymbolRepository
    private lateinit var dispatcherProvider: TestDispatcherProvider
    private lateinit var vm: SymbolViewModel

    @Before
    fun setup() {
        repo = mockk()
        dispatcherProvider = TestDispatcherProvider(mainRule.scheduler)
        vm = SymbolViewModel(repo, dispatcherProvider)
    }

    @Test
    fun `load success - updates symbols and clears error`() =
        runTest(mainRule.scheduler) {
            // 準備
            val entities = listOf(
                Symbol("AAPL", "Apple Inc."),
                Symbol("GOOG", "Alphabet Inc.")
            )
            val expected = listOf(
                SymbolItem("AAPL", "Apple Inc."),
                SymbolItem("GOOG", "Alphabet Inc.")
            )
            coEvery { repo.fetchSymbols() } returns entities

            // 実行
            vm.load()
            advanceUntilIdle()

            // 検証
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorResId).isNull()
            assertThat(state.symbols).isEqualTo(expected)
            coVerify(exactly = 1) { repo.fetchSymbols() }
        }

    @Test
    fun `load failure with IOException - sets network error`() =
        runTest(mainRule.scheduler) {
            // 準備
            coEvery { repo.fetchSymbols() } throws IOException("Network down")

            // 実行
            vm.load()
            advanceUntilIdle()

            // 検証
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.symbols).isEmpty()
            assertThat(state.errorResId).isEqualTo(R.string.error_network)
            coVerify(exactly = 1) { repo.fetchSymbols() }
        }

    @Test
    fun `load failure with HttpException - sets server error`() =
        runTest(mainRule.scheduler) {
            // 準備
            val response = Response.error<Any>(500, okhttp3.ResponseBody.create(null, ""))
            coEvery { repo.fetchSymbols() } throws HttpException(response)

            // 実行
            vm.load()
            advanceUntilIdle()

            // 検証
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorResId).isEqualTo(R.string.error_server)
        }

    @Test
    fun `load failure with SerializationException - sets json error`() =
        runTest(mainRule.scheduler) {
            // 準備
            coEvery { repo.fetchSymbols() } throws SerializationException("Invalid JSON")

            // 実行
            vm.load()
            advanceUntilIdle()

            // 検証
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorResId).isEqualTo(R.string.error_json)
        }

    @Test
    fun `load failure with unknown exception - sets unknown error`() =
        runTest(mainRule.scheduler) {
            // 準備
            coEvery { repo.fetchSymbols() } throws RuntimeException("Unknown")

            // 実行
            vm.load()
            advanceUntilIdle()

            // 検証
            val state = vm.ui.value
            assertThat(state.errorResId).isEqualTo(R.string.error_unknown)
        }

    @Test
    fun `load clears previous error on new request start`() = runTest(mainRule.scheduler) {
        // 準備 - 最初のリクエストが失敗
        coEvery { repo.fetchSymbols() } throws IOException("first failure")
        vm.load()
        advanceUntilIdle()
        assertThat(vm.ui.value.errorResId).isEqualTo(R.string.error_network)

        // 準備 - 2回目のリクエストが成功
        val entities = listOf(Symbol("MSFT", "Microsoft"))
        val expected = listOf(SymbolItem("MSFT", "Microsoft"))
        coEvery { repo.fetchSymbols() } returns entities

        // 実行
        vm.load()
        runCurrent()

        // 検証 - リクエスト開始時にエラーがクリアされること
        assertThat(vm.ui.value.errorResId).isNull()

        advanceUntilIdle()
        assertThat(vm.ui.value.symbols).isEqualTo(expected)
        assertThat(vm.ui.value.isLoading).isFalse()
    }
}