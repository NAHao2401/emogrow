package com.example.emogrow.data.remote.dto.journal

data class DiaryCreateRequest(
    val emotion_name: String,
    val emotion_emoji: String,
    val plant_state: String,
    val feeling_note: String? = null,
    val seed_color: String? = null
)
