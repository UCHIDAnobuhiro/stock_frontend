package com.example.stock.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 認証API通信を定義するRetrofitインターフェース。
 *
 * ログインリクエストを送信してトークンを受信、またはサインアップリクエストを送信して登録結果メッセージを受信する。
 */
interface AuthApi {
    /**
     * ログインAPIエンドポイントを呼び出す。
     *
     * @param body メールアドレスとパスワードを含むログインリクエスト
     * @return トークンを含む認証結果
     */
    @POST("v1/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    /**
     * サインアップAPIエンドポイントを呼び出す。
     *
     * @param body メールアドレスとパスワードを含むサインアップリクエスト
     * @return メッセージを含むサインアップ結果
     */
    @POST("v1/signup")
    suspend fun signup(@Body body: SignupRequest): SignupResponse

}