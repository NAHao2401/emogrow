package com.example.emogrow.data.remote.dto.emotion

data class ChildFlashcardProgressResponse(
    val progress_id: Int,
    val child_id: Int,
    val flashcard_id: Int,

    val viewed_count: Int,
    val flip_count: Int,
    val explanation_viewed_count: Int,

    val is_completed: Boolean,
    val completed_at: String?,
    val last_viewed_at: String?,

    val flashcard: EmotionFlashcardResponse?
)