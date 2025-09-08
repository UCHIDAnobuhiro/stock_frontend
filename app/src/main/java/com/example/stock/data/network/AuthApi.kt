package com.example.stock.data.network

import com.example.stock.data.model.LoginRequest
import com.example.stock.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

}