package com.example.emogrow.features.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.remote.ApiErrorParser
import com.example.emogrow.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                repository.login(email, password)
                _uiState.value = AuthUiState(isLoginSuccess = true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Please fill in all fields")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = AuthUiState(errorMessage = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            try {
                repository.register(fullName, email, password)
                _uiState.value = AuthUiState(isRegisterSuccess = true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun resetState() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState(
            errorMessage = message
        )
    }
}