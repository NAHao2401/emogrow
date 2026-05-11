package com.example.emogrow.data.remote.dto.journal

data class DiaryResponse(
    val diary_id: Int,
    val child_id: Int,
    val emotion_name: String,
    val emotion_emoji: String,
    val emotion_color: String?,
    val plant_state: String,
    val feeling_note: String?,
    val diary_date: String?,
    val created_at: String
)
