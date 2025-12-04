package com.example.stock.core.data.auth

/**
 * アクセストークンの取得・更新・クリアを行うためのインターフェース。
 */
interface TokenProvider {
    /**
     * 現在保持しているトークンを取得する。
     * @return 保持中のトークン。なければnull。
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
}

/**
 * メモリ上でのみトークンを管理するTokenProvider実装。
 * スレッドセーフな@Volatile変数でトークンを保持する。
 */
class InMemoryTokenProvider : TokenProvider {
    @Volatile
    private var token: String? = null // メモリ上のトークン

    /**
     * 保持中のトークンを返す。
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
}