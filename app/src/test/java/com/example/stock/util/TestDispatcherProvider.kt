package com.example.stock.util

import com.example.stock.core.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Test implementation of [DispatcherProvider] for unit testing.
 *
 * Uses [StandardTestDispatcher] for all dispatchers, enabling deterministic
 * control over coroutine execution in tests.
 *
 * @param scheduler The test scheduler to use for all dispatchers.
 *                  If not provided, a new scheduler is created.
 */
class TestDispatcherProvider(
    scheduler: TestCoroutineScheduler = TestCoroutineScheduler()
) : DispatcherProvider {

    private val testDispatcher = StandardTestDispatcher(scheduler)

    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}
