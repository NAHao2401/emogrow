package com.example.emogrow.data.remote.dto

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String
)