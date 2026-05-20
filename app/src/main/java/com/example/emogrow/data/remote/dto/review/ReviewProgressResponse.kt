package com.example.emogrow.data.remote.dto.review

data class ReviewProgressResponse(
    val read_book_ids: List<String>,
    val unlocked_sticker_ids: List<String>
)
