package com.example.emogrow.data.remote.dto.review

data class EmotionLogRequest(
    val emotion_type: String,
    val intensity: Int,
    val note: String? = null,
    val source: String = "journal" // "journal" or "lesson"
)
