package com.example.emogrow.data.remote.dto.emotion

data class EmotionResponse(
    val emotion_id: Int,
    val name: String,
    val description: String?,
    val color_code: String?,
    val emoji: String?,
    val audio_url: String?,
    val animation_url: String?
)