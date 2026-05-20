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

    fun checkAutoLogin() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(
                isCheckingAuth = true,
                hasCheckedAuth = false
            )

            val isValidToken = repository.checkAutoLogin()

            _uiState.value = AuthUiState(
                isCheckingAuth = false,
                hasCheckedAuth = true,
                isAuthenticated = isValidToken
            )
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val user = repository.getCurrentUser()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUser = user
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

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

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Vui lòng nhập đầy đủ thông tin",
                    successMessage = null,
                    isChangePasswordSuccess = false
                )
            }
            return
        }

        if (newPassword.length < 6) {
            _uiState.update {
                it.copy(
                    errorMessage = "Mật khẩu mới phải có ít nhất 6 ký tự",
                    successMessage = null,
                    isChangePasswordSuccess = false
                )
            }
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.update {
                it.copy(
                    errorMessage = "Mật khẩu xác nhận không khớp",
                    successMessage = null,
                    isChangePasswordSuccess = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                    isChangePasswordSuccess = false
                )
            }

            try {
                repository.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isChangePasswordSuccess = true,
                        successMessage = "Đổi mật khẩu thành công"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.parse(e),
                        successMessage = null,
                        isChangePasswordSuccess = false
                    )
                }
            }
        }
    }

    fun resetChangePasswordState() {
        _uiState.update {
            it.copy(
                isChangePasswordSuccess = false,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()

            _uiState.value = AuthUiState(
                isAuthenticated = false,
                isLoggedOut = true
            )
        }
    }

    fun resetState() {
        _uiState.update {
            it.copy(
                isLoginSuccess = false,
                isRegisterSuccess = false,
                isLoggedOut = false,
                isChangePasswordSuccess = false,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState(
            errorMessage = message
        )
    }
}