package com.example.emogrow.data.remote.dto

data class ErrorResponse(
    val success: Boolean,
    val message: String,
    val error_code: String?,
    val details: Any?
)