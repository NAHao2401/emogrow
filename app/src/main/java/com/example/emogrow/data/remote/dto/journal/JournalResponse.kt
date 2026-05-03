package com.example.emogrow.data.remote.dto.journal

data class JournalResponse(
    val journal_id: Int,
    val child_id: Int,
    val emotion: String,
    val note: String?,
    val audio_url: String?,
    val created_at: String
)
