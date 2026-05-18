package com.example.emogrow.features.auth.viewmodel

import com.example.emogrow.data.remote.dto.UserResponse

data class AuthUiState(
    val isLoading: Boolean = false,
    val isCheckingAuth: Boolean = false,
    val hasCheckedAuth: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val isLoggedOut: Boolean = false,
    val currentUser: UserResponse? = null,
    val errorMessage: String? = null
)