package com.example.emogrow.data.remote.dto.progress

data class GameProgressResponse(
    val progress_id: Int,
    val child_id: Int,
    val last_passed_level: Int
)