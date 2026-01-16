package com.example.stock.feature.stocklist.viewmodel

import com.example.stock.R
import com.example.stock.feature.stocklist.data.remote.SymbolDto
import com.example.stock.feature.stocklist.data.repository.SymbolRepository
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
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SymbolViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var repo: SymbolRepository
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
                SymbolDto("AAPL", "Apple Inc."),
                SymbolDto("GOOG", "Alphabet Inc.")
            )
            coEvery { repo.fetchSymbols() } returns expected

            // when
            vm.load()
            advanceUntilIdle()

            // then
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorResId).isNull()
            assertThat(state.symbols).isEqualTo(expected)
            coVerify(exactly = 1) { repo.fetchSymbols() }
        }

    @Test
    fun `load failure with IOException - sets network error`() =
        runTest(mainRule.scheduler) {
            // given
            coEvery { repo.fetchSymbols() } throws IOException("Network down")

            // when
            vm.load()
            advanceUntilIdle()

            // then
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.symbols).isEmpty()
            assertThat(state.errorResId).isEqualTo(R.string.error_network)
            coVerify(exactly = 1) { repo.fetchSymbols() }
        }

    @Test
    fun `load failure with HttpException - sets server error`() =
        runTest(mainRule.scheduler) {
            // given
            val response = Response.error<Any>(500, okhttp3.ResponseBody.create(null, ""))
            coEvery { repo.fetchSymbols() } throws HttpException(response)

            // when
            vm.load()
            advanceUntilIdle()

            // then
            val state = vm.ui.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.errorResId).isEqualTo(R.string.error_server)
        }

    @Test
    fun `load failure with unknown exception - sets unknown error`() =
        runTest(mainRule.scheduler) {
            // given
            coEvery { repo.fetchSymbols() } throws RuntimeException("Unknown")

            // when
            vm.load()
            advanceUntilIdle()

            // then
            val state = vm.ui.value
            assertThat(state.errorResId).isEqualTo(R.string.error_unknown)
        }

    @Test
    fun `load clears previous error on new request start`() = runTest(mainRule.scheduler) {
        // given - first request fails
        coEvery { repo.fetchSymbols() } throws IOException("first failure")
        vm.load()
        advanceUntilIdle()
        assertThat(vm.ui.value.errorResId).isEqualTo(R.string.error_network)

        // given - second request succeeds
        val expected = listOf(SymbolDto("MSFT", "Microsoft"))
        coEvery { repo.fetchSymbols() } returns expected

        // when
        vm.load()
        runCurrent()

        // then - error is cleared on request start
        assertThat(vm.ui.value.errorResId).isNull()

        advanceUntilIdle()
        assertThat(vm.ui.value.symbols).isEqualTo(expected)
        assertThat(vm.ui.value.isLoading).isFalse()
    }
}