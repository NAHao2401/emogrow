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
}