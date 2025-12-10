package com.example.stock.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for providing coroutine dispatchers.
 *
 * This abstraction allows for easy testing by enabling the injection
 * of test dispatchers in unit tests while using real dispatchers in production.
 */
interface DispatcherProvider {
    /** Main dispatcher for UI operations */
    val main: CoroutineDispatcher

    /** IO dispatcher for disk and network operations */
    val io: CoroutineDispatcher

    /** Default dispatcher for CPU-intensive operations */
    val default: CoroutineDispatcher
}

/**
 * Default implementation of [DispatcherProvider] that uses the standard Android dispatchers.
 *
 * This implementation is used in production and provides the real dispatchers:
 * - [Dispatchers.Main] for UI operations
 * - [Dispatchers.IO] for disk and network operations
 * - [Dispatchers.Default] for CPU-intensive operations
 */
@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
