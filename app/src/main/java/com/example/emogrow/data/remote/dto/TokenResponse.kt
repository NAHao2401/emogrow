package com.example.emogrow.data.remote.dto

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val user: UserResponse
)