package com.example.emogrow.features.emotions.viewmodel

import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionFlashcardResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionResponse

data class EmotionUiState(
    val isLoading: Boolean = false,

    val emotions: List<EmotionResponse> = emptyList(),
    val flashcards: List<EmotionFlashcardResponse> = emptyList(),
    val progressList: List<ChildFlashcardProgressResponse> = emptyList(),

    val selectedEmotion: EmotionResponse? = null,
    val selectedFlashcard: EmotionFlashcardResponse? = null,
    val selectedProgress: ChildFlashcardProgressResponse? = null,

    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)