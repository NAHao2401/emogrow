package com.example.emogrow.data.remote.dto.review

data class EmotionLogResponse(
    val emotion_log_id: Int,
    val child_id: Int,
    val emotion_type: String,
    val intensity: Int,
    val audio_url: String?,
    val note: String?,
    val source: String?,
    val created_at: String
)
