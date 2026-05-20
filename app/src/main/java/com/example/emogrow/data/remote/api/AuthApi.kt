package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.ChangePasswordRequest
import com.example.emogrow.data.remote.dto.LoginRequest
import com.example.emogrow.data.remote.dto.RegisterRequest
import com.example.emogrow.data.remote.dto.TokenResponse
import com.example.emogrow.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): UserResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): TokenResponse

    @GET("auth/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): UserResponse

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    )
}