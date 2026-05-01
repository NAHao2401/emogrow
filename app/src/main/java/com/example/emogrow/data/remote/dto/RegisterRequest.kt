package com.example.emogrow.data.remote.dto

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)