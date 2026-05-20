package com.example.emogrow.data.remote.dto.review

data class EmotionStatisticItem(
    val emotion_type: String,
    val count: Int,
    val average_intensity: Double,
    val color_code: String? = null
)
