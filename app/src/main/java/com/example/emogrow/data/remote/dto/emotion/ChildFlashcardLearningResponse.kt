package com.example.emogrow.data.remote.dto.emotion

data class ChildFlashcardLearningResponse(
    val flashcard: EmotionFlashcardResponse,
    val progress: ChildFlashcardProgressResponse?
)