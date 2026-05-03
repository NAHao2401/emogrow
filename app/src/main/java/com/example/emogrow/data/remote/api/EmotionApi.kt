package com.example.emogrow.data.remote.api

import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardLearningResponse
import com.example.emogrow.data.remote.dto.emotion.ChildFlashcardProgressResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionFlashcardResponse
import com.example.emogrow.data.remote.dto.emotion.EmotionResponse
import com.example.emogrow.data.remote.dto.emotion.FlashcardInteractionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface EmotionApi {

    @GET("emotions")
    suspend fun getEmotions(): List<EmotionResponse>

    @GET("emotions/flashcards")
    suspend fun getFlashcards(): List<EmotionFlashcardResponse>

    @GET("emotions/{emotionId}/flashcards")
    suspend fun getFlashcardsByEmotion(
        @Path("emotionId") emotionId: Int
    ): List<EmotionFlashcardResponse>

    @POST("emotions/flashcards/view")
    suspend fun viewFlashcard(
        @Header("Authorization") token: String,
        @Body request: FlashcardInteractionRequest
    ): ChildFlashcardLearningResponse

    @POST("emotions/flashcards/flip")
    suspend fun flipFlashcard(
        @Header("Authorization") token: String,
        @Body request: FlashcardInteractionRequest
    ): ChildFlashcardProgressResponse

    @POST("emotions/flashcards/explanation")
    suspend fun viewExplanation(
        @Header("Authorization") token: String,
        @Body request: FlashcardInteractionRequest
    ): ChildFlashcardProgressResponse

    @POST("emotions/flashcards/complete")
    suspend fun completeFlashcard(
        @Header("Authorization") token: String,
        @Body request: FlashcardInteractionRequest
    ): ChildFlashcardProgressResponse

    @GET("emotions/children/{childId}/progress")
    suspend fun getChildProgress(
        @Header("Authorization") token: String,
        @Path("childId") childId: Int
    ): List<ChildFlashcardProgressResponse>
}