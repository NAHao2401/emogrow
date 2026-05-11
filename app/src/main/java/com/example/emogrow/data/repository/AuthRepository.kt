package com.example.emogrow.data.repository

import android.content.Context
import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.AuthApi
import com.example.emogrow.data.remote.dto.LoginRequest
import com.example.emogrow.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.first

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ) = authApi.register(
        RegisterRequest(
            full_name = fullName,
            email = email,
            password = password
        )
    )

    suspend fun login(
        email: String,
        password: String
    ) {
        val response = authApi.login(
            LoginRequest(
                email = email,
                password = password
            )
        )

        tokenManager.saveToken(response.access_token)
    }

    suspend fun checkAutoLogin(): Boolean {
        val token = tokenManager.accessToken.first()

        if (token.isNullOrBlank()) {
            return false
        }

        return try {
            authApi.getMe("Bearer $token")
            true
        } catch (e: Exception) {
            tokenManager.clearToken()
            false
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}