package com.example.emogrow.data.remote.dto.journal

data class JournalCreateRequest(
    val emotion: String,
    val note: String? = null
)
