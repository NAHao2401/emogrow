package com.example.emogrow.data.remote.dto

data class EmotionResponse(
    val emotion_id: Int,
    val name: String,
    val description: String,
    val emoji: String?,
    val image_url: String?
)
