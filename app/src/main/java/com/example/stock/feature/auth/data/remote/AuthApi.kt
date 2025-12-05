package com.example.stock.feature.auth.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface defining authentication API communication.
 *
 * Sends login/signup requests and receives responses containing authentication tokens.
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

    /**
     * Calls the signup API endpoint.
     *
     * @param body Signup request containing email and password
     * @return Authentication result containing token
     */
    @POST("signup")
    suspend fun signup(@Body body: SignupRequest): SignupResponse

}