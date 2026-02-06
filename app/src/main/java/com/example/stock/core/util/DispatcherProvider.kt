package com.example.stock.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * コルーチンディスパッチャーを提供するインターフェース。
 *
 * この抽象化により、本番環境では実際のディスパッチャーを使用しながら、
 * ユニットテストではテスト用ディスパッチャーを注入することでテストが容易になる。
 */
interface DispatcherProvider {
    /** UI操作用のMainディスパッチャー */
    val main: CoroutineDispatcher

    /** ディスクとネットワーク操作用のIOディスパッチャー */
    val io: CoroutineDispatcher

    /** CPU負荷の高い操作用のDefaultディスパッチャー */
    val default: CoroutineDispatcher
}

/**
 * 標準のAndroidディスパッチャーを使用する[DispatcherProvider]のデフォルト実装。
 *
 * この実装は本番環境で使用され、実際のディスパッチャーを提供する：
 * - UI操作用の[Dispatchers.Main]
 * - ディスクとネットワーク操作用の[Dispatchers.IO]
 * - CPU負荷の高い操作用の[Dispatchers.Default]
 */
@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
