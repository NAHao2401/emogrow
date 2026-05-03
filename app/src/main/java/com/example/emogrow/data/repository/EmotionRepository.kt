package com.example.emogrow.data.repository

import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.EmotionApi
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardLearningResponse
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionFlashcardResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionResponse
import com.example.emogrow.data.remote.dto.emotion.FlashcardInteractionRequest
import kotlinx.coroutines.flow.first

class EmotionRepository(
    private val emotionApi: EmotionApi,
    private val tokenManager: TokenManager
) {
    private suspend fun getBearerToken(): String {
        val token = tokenManager.accessToken.first()
        return "Bearer $token"
    }

    suspend fun getEmotions(): List<EmotionResponse> {
        return emotionApi.getEmotions()
    }

    suspend fun getFlashcards(): List<EmotionFlashcardResponse> {
        return emotionApi.getFlashcards()
    }

    suspend fun getFlashcardsByEmotion(emotionId: Int): List<EmotionFlashcardResponse> {
        return emotionApi.getFlashcardsByEmotion(emotionId)
    }

    suspend fun viewFlashcard(
        childId: Int,
        flashcardId: Int
    ): ChildFlashcardLearningResponse {
        return emotionApi.viewFlashcard(
            token = getBearerToken(),
            request = FlashcardInteractionRequest(
                child_id = childId,
                flashcard_id = flashcardId
            )
        )
    }

    suspend fun flipFlashcard(
        childId: Int,
        flashcardId: Int
    ): ChildFlashcardProgressResponse {
        return emotionApi.flipFlashcard(
            token = getBearerToken(),
            request = FlashcardInteractionRequest(
                child_id = childId,
                flashcard_id = flashcardId
            )
        )
    }

    suspend fun viewExplanation(
        childId: Int,
        flashcardId: Int
    ): ChildFlashcardProgressResponse {
        return emotionApi.viewExplanation(
            token = getBearerToken(),
            request = FlashcardInteractionRequest(
                child_id = childId,
                flashcard_id = flashcardId
            )
        )
    }

    suspend fun completeFlashcard(
        childId: Int,
        flashcardId: Int
    ): ChildFlashcardProgressResponse {
        return emotionApi.completeFlashcard(
            token = getBearerToken(),
            request = FlashcardInteractionRequest(
                child_id = childId,
                flashcard_id = flashcardId
            )
        )
    }

    suspend fun getChildProgress(childId: Int): List<ChildFlashcardProgressResponse> {
        return emotionApi.getChildProgress(
            token = getBearerToken(),
            childId = childId
        )
    }
}