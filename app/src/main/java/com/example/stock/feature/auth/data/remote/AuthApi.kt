package com.example.stock.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface defining authentication API communication.
 *
 * Sends login requests and receives responses containing authentication tokens.
 */
interface AuthApi {
    /**
     * Calls the login API endpoint.
     *
     * @param body Login request containing email and password
     * @return Authentication result containing token
     */
    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

}