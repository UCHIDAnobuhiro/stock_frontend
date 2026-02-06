package com.example.stock.core.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * アクセストークンの取得・更新・クリアを行うインターフェース。
 */
interface TokenProvider {
    /**
     * 現在保持しているトークンを取得する。
     * @return 保持しているトークン。存在しない場合はnull。
     */
    fun getToken(): String?

    /**
     * トークンを新しい値で更新する。
     * @param token 新しいトークン
     */
    fun update(token: String)

    /**
     * 保持しているトークンをクリアする。
     */
    fun clear()

    /**
     * ストレージからのトークン復元が完了したかを示すStateFlow。
     */
    val isRestorationComplete: StateFlow<Boolean>

    /**
     * トークン復元完了をマークする。
     * 永続ストレージからトークンを復元した後にApplicationから呼び出される。
     */
    fun markRestorationComplete()

    /**
     * トークン復元が完了するまで中断する。
     */
    suspend fun awaitRestoration()
}

/**
 * メモリ上でのみトークンを管理するTokenProvider実装。
 * スレッドセーフな@Volatile変数でトークンを保持する。
 */
class InMemoryTokenProvider : TokenProvider {
    @Volatile
    private var token: String? = null

    private val _isRestorationComplete = MutableStateFlow(false)
    override val isRestorationComplete: StateFlow<Boolean> = _isRestorationComplete.asStateFlow()

    /**
     * 保持しているトークンを返す。
     */
    override fun getToken(): String? = token

    /**
     * トークンを新しい値で上書きする。
     */
    override fun update(token: String) {
        this.token = token
    }

    /**
     * トークンをクリアする。
     */
    override fun clear() {
        this.token = null
    }

    /**
     * トークン復元完了をマークする。
     */
    override fun markRestorationComplete() {
        _isRestorationComplete.value = true
    }

    /**
     * トークン復元が完了するまで中断する。
     */
    override suspend fun awaitRestoration() {
        _isRestorationComplete.first { it }
    }
}