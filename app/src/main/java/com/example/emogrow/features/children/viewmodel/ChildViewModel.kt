package com.example.emogrow.features.children.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.remote.ApiErrorParser
import com.example.emogrow.data.repository.ChildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChildViewModel(
    private val repository: ChildRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState: StateFlow<ChildUiState> = _uiState

    fun loadMyChildren() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val children = repository.getMyChildren()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    children = children
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Không tải được hồ sơ trẻ"
                )
            }
        }
    }

    fun createChild(
        nickname: String,
        age: Int,
        avatarUrl: String?,
        accessibilityNeeds: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isSuccess = false)

            try {
                repository.createChild(nickname, age, avatarUrl, accessibilityNeeds)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                loadMyChildren()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun loadChildById(childId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val child = repository.getChildById(childId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedChild = child
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isSuccess = false
        )
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message,
            isSuccess = false
        )
    }
}