package com.example.stock.data.network

import com.example.stock.data.model.LoginRequest
import com.example.stock.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 認証APIとの通信を定義するRetrofitインターフェース。
 *
 * ログインリクエストを送信し、認証トークン等を含むレスポンスを受け取る。
 */
interface AuthApi {
    /**
     * ログインAPIを呼び出す。
     *
     * @param body ログインリクエスト（メールアドレス・パスワード等）
     * @return 認証結果（トークン等）
     */
    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

}