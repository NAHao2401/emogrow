package com.example.emogrow.features.review.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emogrow.data.remote.ApiErrorParser
import com.example.emogrow.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState

    fun loadStatistics(childId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = repository.getEmotionStatistics(childId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    statistics = response,
                    totalCount = response.total_count,
                    distribution = response.distribution,
                    averageIntensity = response.average_intensity
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}