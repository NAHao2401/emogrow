package com.example.emogrow.features.auth.viewmodel

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val errorMessage: String? = null
)