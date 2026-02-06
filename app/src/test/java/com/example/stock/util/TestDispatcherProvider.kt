package com.example.stock.util

import com.example.stock.core.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * ユニットテスト用の[DispatcherProvider]テスト実装。
 *
 * すべてのディスパッチャに[StandardTestDispatcher]を使用し、
 * テストでのコルーチン実行を決定論的に制御できるようにする。
 *
 * @param scheduler すべてのディスパッチャに使用するテストスケジューラ。
 *                  指定しない場合は新しいスケジューラが作成される。
 */
class TestDispatcherProvider(
    scheduler: TestCoroutineScheduler = TestCoroutineScheduler()
) : DispatcherProvider {

    private val testDispatcher = StandardTestDispatcher(scheduler)

    override val main: CoroutineDispatcher = testDispatcher
    override val io: CoroutineDispatcher = testDispatcher
    override val default: CoroutineDispatcher = testDispatcher
}
