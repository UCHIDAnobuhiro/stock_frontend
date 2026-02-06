package com.example.stock.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 認証API通信を定義するRetrofitインターフェース。
 *
 * ログイン/サインアップリクエストを送信し、認証トークンを含むレスポンスを受信する。
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
     * @return トークンを含む認証結果
     */
    @POST("v1/signup")
    suspend fun signup(@Body body: SignupRequest): SignupResponse

}