package com.example.emogrow.data.remote.dto

data class UserResponse(
    val user_id: Int,
    val full_name: String,
    val email: String,
    val role: String
)