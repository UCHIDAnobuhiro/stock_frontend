package com.example.stock.data.auth

interface TokenProvider {
    fun getToken(): String?
    fun update(token: String)
    fun clear()
}

class InMemoryTokenProvider : TokenProvider {
    @Volatile
    private var token: String? = null

    override fun getToken(): String? = token

    override fun update(token: String) {
        this.token = token
    }

    override fun clear() {
        this.token = null
    }
}