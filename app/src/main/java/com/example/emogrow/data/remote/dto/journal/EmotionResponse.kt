package com.example.emogrow.data.remote.dto.journal

data class EmotionResponse(
    val emotion_id: Int,
    val name: String,
    val emoji: String,
    val color_code: String
)
