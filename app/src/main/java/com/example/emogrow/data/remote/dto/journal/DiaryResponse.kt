package com.example.emogrow.data.remote.dto.journal

data class DiaryResponse(
    val diary_id: Int,
    val child_id: Int,
    val emotion_id: Int,
    val emotion_name: String?, // Tên cảm xúc lấy từ bảng emotions
    val emotion_emoji: String?, // Emoji lấy từ bảng emotions
    val diary_date: String?,
    val seed_color: String?,
    val plant_state: String,
    val feeling_note: String?,
    val voice_url: String?,
    val created_at: String
)
