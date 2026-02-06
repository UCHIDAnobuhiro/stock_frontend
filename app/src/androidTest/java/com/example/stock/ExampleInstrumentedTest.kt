package com.example.stock

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Androidデバイス上で実行されるインストルメンテッドテスト。
 *
 * [テストドキュメント](http://d.android.com/tools/testing)を参照。
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // テスト対象アプリのContext。
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.todoapp", appContext.packageName)
    }
}