package com.example.emogrow.data.remote.dto.review

data class EmotionStatisticsResponse(
    val total_count: Int,
    val distribution: List<EmotionStatisticItem>,
    val average_intensity: Double
)