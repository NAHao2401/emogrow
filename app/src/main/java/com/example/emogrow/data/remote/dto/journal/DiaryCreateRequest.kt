package com.example.emogrow.data.remote.dto.journal

data class DiaryCreateRequest(
    val emotion_id: Int,
    val diary_date: String,
    val seed_color: String?,
    val plant_state: String,
    val feeling_note: String?,
    val voice_url: String?
)
