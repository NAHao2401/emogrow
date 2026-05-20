package com.example.emogrow.data.repository

import com.example.emogrow.data.local.TokenManager
import com.example.emogrow.data.remote.api.ReviewApi
import com.example.emogrow.data.remote.dto.review.EmotionLogResponse
import com.example.emogrow.data.remote.dto.review.EmotionStatisticsResponse
import com.example.emogrow.data.remote.dto.review.StickerCollectionResponse
import kotlinx.coroutines.flow.first

class ReviewRepository(
    private val reviewApi: ReviewApi,
    private val tokenManager: TokenManager
) {
    private suspend fun getBearerToken(): String {
        val token = tokenManager.accessToken.first()
        return "Bearer $token"
    }

    suspend fun getEmotionStatistics(childId: Int): EmotionStatisticsResponse {
        return reviewApi.getEmotionStatistics(
            token = getBearerToken(),
            childId = childId
        )
    }

    suspend fun getEmotionLogs(childId: Int): List<EmotionLogResponse> {
        return reviewApi.getEmotionLogs(
            token = getBearerToken(),
            childId = childId
        )
    }

    suspend fun getStickers(childId: Int): List<StickerCollectionResponse> {
        return reviewApi.getStickers(
            token = getBearerToken(),
            childId = childId
        )
    }

    suspend fun createEmotionLog(
        childId: Int,
        emotionType: String,
        intensity: Int,
        note: String? = null,
        source: String = "journal"
    ): EmotionLogResponse {
        return reviewApi.createEmotionLog(
            token = getBearerToken(),
            childId = childId,
            request = com.example.emogrow.data.remote.dto.review.EmotionLogRequest(
                emotion_type = emotionType,
                intensity = intensity,
                note = note,
                source = source
            )
        )
    }

    suspend fun markBookAsRead(childId: Int, bookId: String) {
        reviewApi.markBookAsRead(
            token = getBearerToken(),
            childId = childId,
            bookId = bookId
        )
    }

    suspend fun getReviewProgress(childId: Int): com.example.emogrow.data.remote.dto.review.ReviewProgressResponse {
        return reviewApi.getReviewProgress(
            token = getBearerToken(),
            childId = childId
        )
    }
}