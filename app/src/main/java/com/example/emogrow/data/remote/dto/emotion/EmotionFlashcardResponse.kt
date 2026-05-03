package com.example.emogrow.data.remote.dto.emotion

data class EmotionFlashcardResponse(
    val flashcard_id: Int,
    val emotion_id: Int,

    val title: String,

    val front_text: String,
    val front_instruction: String?,

    val back_title: String?,
    val back_description: String?,

    val explanation: String?,
    val example_situation: String?,

    val audio_url: String?,

    val difficulty_level: Int,
    val is_active: Boolean,

    val emotion: EmotionResponse?
)