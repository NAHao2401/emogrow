package com.example.emogrow.features.review.viewmodel

import com.example.emogrow.data.remote.dto.review.EmotionStatisticItem
import com.example.emogrow.data.remote.dto.review.EmotionStatisticsResponse

data class ReviewUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statistics: EmotionStatisticsResponse? = null,
    val totalCount: Int = 0,
    val distribution: List<EmotionStatisticItem> = emptyList(),
    val averageIntensity: Double = 0.0
)